package chunk;

import entity.EntityManager;
import entity.ModelComponent;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Iterator;

public class ChunkLoader {

    private static final int LOAD_RADIUS = 6 +2;
    private static HashMap<Vector3i, Chunk> chunks = new HashMap<>();

    public static int update(Vector3f playerPos) {
        Vector3i playerChunkPos = Chunk.worldPosToChunkPos(playerPos);
        chunks.values().forEach(chunk -> chunk.updated = false);

        // update chunks
        int updatedCount = 0;
        // start from the inside and loop outside
        for (int radius = 0; radius < LOAD_RADIUS; radius++)
        for (int x = playerChunkPos.x - radius; x < playerChunkPos.x + radius +1; x++)
        for (int y = playerChunkPos.y - radius; y < playerChunkPos.y + radius +1; y++)
        for (int z = playerChunkPos.z - radius; z < playerChunkPos.z + radius +1; z++)
        {
            if (doUpdateChunk(x, y, z)) {
                updatedCount++;
            }
        }

        // unload chunks
        var it = chunks.values().iterator();
        while (it.hasNext()) {
            var chunk = it.next();
            if (!chunk.updated) {
                unloadChunk(chunk, it);
            }
        }

        return updatedCount;
    }

    private static boolean doUpdateChunk(int x, int y, int z) {
        var pos = new Vector3i(x, y, z);
        var chunk = chunks.get(pos);
        if (chunk == null) {
            chunk = new Chunk(pos);
            chunks.put(pos, chunk);
        }

        chunk.updated = true;

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
        switch (chunk.getStatus()) {
            case NONE -> {
                if (hasAllUnloadedNeighbors(chunk)) {
                    it.remove();
                }
            }

            case WAIT_NEIGHBORS, LOADED -> chunk.setStatus(Chunk.Status.NONE);
            case PREPARED -> {
                EntityManager.removeComponent(chunk, ChunkModelDataComponent.class);
                chunk.setStatus(Chunk.Status.NONE);
            }
            case FINAL -> {
                var comp = EntityManager.removeComponent(chunk, ModelComponent.class);
                if (comp != null) {
                    comp.getModel().destroy();
                }
                EntityManager.removeComponent(chunk, TransformationComponent.class);
                chunk.setStatus(Chunk.Status.NONE);
            }
        }
    }

    public static byte getBlockAt(Vector3f pos) {
        var chunkPos = Chunk.worldPosToChunkPos(pos);
        var chunkRemainderPos = Chunk.worldPosToBlockPos(pos);

        var chunk = chunks.get(chunkPos);
        if (chunk == null || chunk.getStatus().urgency < Chunk.Status.LOADED.urgency) return Block.INVALID.getID();
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
}
