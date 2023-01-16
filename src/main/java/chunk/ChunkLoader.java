package chunk;

import entity.EntityManager;
import entity.ModelComponent;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Iterator;

public class ChunkLoader {

    private static final int LOAD_RADIUS = 16;
    private static HashMap<Vector3i, Chunk> chunks = new HashMap<>();

    public static int update(Vector3f playerPos) {
        Vector3i playerChunkPos = Chunk.worldPosToChunkPos(playerPos);
        int updatedCount = 0;

        var minX = playerChunkPos.x - LOAD_RADIUS;
        var maxX = playerChunkPos.x + LOAD_RADIUS;
        var minY = playerChunkPos.y - LOAD_RADIUS;
        var maxY = playerChunkPos.y + LOAD_RADIUS;
        var minZ = playerChunkPos.z - LOAD_RADIUS;
        var maxZ = playerChunkPos.z + LOAD_RADIUS;

        // load new chunks
        for (int x = minX; x <= maxX; x++)
        for (int y = minY; y <= maxY; y++)
        for (int z = minZ; z <= maxZ; z++) {
            var pos = new Vector3i(x, y, z);
            if (!chunks.containsKey(pos)) {
                var chunk = new Chunk(pos);
                chunks.put(pos, chunk);
                updatedCount++;
            }
        }

        // update existing chunks
        var it = chunks.values().iterator();
        while (it.hasNext()) {
            var chunk = it.next();
            var pos = chunk.getChunkPos();

            if (pos.x < minX || pos.x > maxX ||
                pos.y < minY || pos.y > maxY ||
                pos.z < minZ || pos.z > maxZ) {
                unloadChunk(chunk, it);
            } else {
                if (doUpdateChunk(chunk, pos)) {
                    updatedCount++;
                }
            }
        }

        return updatedCount;
    }

    public static void updateSpoiled() {
        var it = chunks.values().iterator();
        while (it.hasNext()) {
            var chunk = it.next();
            if (chunk.spoiled) {
                chunk.spoiled = false;
                updateNow(chunk);
            }
        }
    }

    // assume this will only ever be called on chunks that are loaded or higher.
    public static void updateNow(Chunk chunk) {
        // generate mesh
        chunk.setStatus(Chunk.Status.MESH_GENERATING);
        EntityManager.removeComponent(chunk, ChunkModelDataComponent.class);
        TerrainModelGenerator.loadChunk(chunk);

        chunk.setStatus(Chunk.Status.MESH_LOADING);
        var oldModel = EntityManager.removeComponent(chunk, ModelComponent.class);
        EntityManager.removeComponent(chunk, TransformationComponent.class);
        TerrainModelLoader.loadChunk(chunk);

        if (oldModel != null) {
            oldModel.getModel().destroy();
        }

        assert chunk.getStatus() == Chunk.Status.FINAL;
    }

    public static int getQueueSize() {
        return TerrainGenerator.getQueueSize()
                + StructureGenerator.getQueueSize()
                + TerrainModelGenerator.getQueueSize()
                + TerrainModelLoader.getQueueSize();
    }

    public static void setBlockAt(Vector3i pos, byte block) {
        var chunkPos = Chunk.worldPosToChunkPos(pos);
        var blockPos = Chunk.worldPosToBlockPos(pos);
        var chunk = chunks.get(chunkPos);
        if (chunk == null || chunk.getStatus().urgency < Chunk.Status.LOADED.urgency) return;
        chunk.setBlockSafe(blockPos, block);
        chunk.spoiled = true;

        if (blockPos.x == 0)            chunk.getNeighbor(DiagonalDirection.LEFT).spoiled = true;
        if (blockPos.x == Chunk.SIZE-1) chunk.getNeighbor(DiagonalDirection.RIGHT).spoiled = true;
        if (blockPos.y == 0)            chunk.getNeighbor(DiagonalDirection.DOWN).spoiled = true;
        if (blockPos.y == Chunk.SIZE-1) chunk.getNeighbor(DiagonalDirection.UP).spoiled = true;
        if (blockPos.z == 0)            chunk.getNeighbor(DiagonalDirection.FRONT).spoiled = true;
        if (blockPos.z == Chunk.SIZE-1) chunk.getNeighbor(DiagonalDirection.BACK).spoiled = true;
    }

    public static byte getBlockAt(Vector3f pos) {
        var chunkPos = Chunk.worldPosToChunkPos(pos);
        var chunkRemainderPos = Chunk.worldPosToBlockPos(pos);

        var chunk = chunks.get(chunkPos);
        if (chunk == null || chunk.getStatus().urgency < Chunk.Status.LOADED.urgency) {
            return Block.INVALID.getID();
        }
        return chunk.getBlock(chunkRemainderPos);
    }

    public static byte getBlockAt(Vector3i pos) {
        return getBlockAt(new Vector3f(pos.x, pos.y, pos.z));
    }

    public static byte getBlockAt(int x, int y, int z) {
        return getBlockAt(new Vector3f(x, y, z));
    }

    public static Chunk getChunkAt(Vector3i pos) {
        return chunks.get(pos);
    }

    public static Chunk getChunkAt(Vector3f pos) {
        return getChunkAt(Chunk.worldPosToChunkPos(pos));
    }

    private static boolean doUpdateChunk(Chunk chunk, Vector3i pos) {
        switch (chunk.getStatus()) {
            case NONE           -> {
                chunk.setStatus(Chunk.Status.TERRAIN_GENERATING);
                TerrainGenerator.addChunks(chunk);
                return true;
            }
            case WAIT_NEIGHBORS -> {
                if (neighborsUrgencyAtLeast(chunk, Chunk.Status.WAIT_NEIGHBORS.urgency)) {
                    chunk.setStatus(Chunk.Status.STRUCTURE_GENERATING);
                    StructureGenerator.addChunk(chunk);
                    return true;
                }
            }
            case LOADED -> {
                if (neighborsUrgencyAtLeast(chunk, Chunk.Status.LOADED.urgency)) {
                    chunk.setStatus(Chunk.Status.MESH_GENERATING);
                    TerrainModelGenerator.addChunk(chunk);
                    return true;
                }
            }
            case PREPARED -> {
                chunk.setStatus(Chunk.Status.MESH_LOADING);
                TerrainModelLoader.addChunk(chunk);
                return true;
            }
        }

        return false;
    }

    private static boolean neighborsUrgencyAtLeast(Chunk chunk, int urgency) {
        for (int dir = 0; dir < DiagonalDirection.COUNT; dir++) {
            var neighbor = chunk.getNeighbor(dir);
            if (neighbor == null || neighbor.getStatus().urgency < urgency) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasAllUnloadedNeighbors(Chunk chunk) {
        for (int dir = 0; dir < DiagonalDirection.COUNT; dir++) {
            var neighbor = chunk.getNeighbor(dir);
            if (neighbor != null && neighbor.getStatus() != Chunk.Status.NONE) return false;
        }
        return true;
    }

    private static void unloadChunk(Chunk chunk, Iterator<Chunk> it) {
        if (chunk.getStatus().working) return; // don't want to unload a chunk that is queued somewhere.

        var modelComp = EntityManager.removeComponent(chunk, ModelComponent.class);
        EntityManager.removeComponent(chunk, ChunkModelDataComponent.class);
        EntityManager.removeComponent(chunk, TransformationComponent.class);

        if (modelComp != null) {
            modelComp.getModel().destroy();
        }

        // this just prevents log spam
        if (chunk.getStatus() != Chunk.Status.NONE)
            chunk.setStatus(Chunk.Status.NONE);

        if (hasAllUnloadedNeighbors(chunk)) {
            it.remove();
        }
    }
}
