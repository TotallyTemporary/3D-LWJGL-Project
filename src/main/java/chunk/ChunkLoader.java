package chunk;

import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Iterator;

/** This class stores and handles the loading of chunks.
 * It loads chunks by sending the chunks to various workers to be processed. */
public class ChunkLoader {

    // if these variables are to be user controlled, they should have 2-3 chunks added to them, to enable the player to always be in a fully loaded chunk.
    private static final int HORIZONTAL_LOAD_RADIUS = 12;
    private static final int VERTICAL_LOAD_RADIUS = 6;

    // a chunk within this grid distance of the player's chunk will instantly get updated
    private static final int INSTANT_LOAD_DISTANCE = 2;
    private static final int INSTANT_LOAD_DISTANCE_SQR = INSTANT_LOAD_DISTANCE*INSTANT_LOAD_DISTANCE;

    private static final HashMap<Vector3i, Chunk> chunks = new HashMap<>();

    /** Updates chunks based on the player's position in the world.
     * @return number of chunks whose status was changed this frame. */
    public static int update(Vector3f playerPos) {
        Vector3i playerChunkPos = Chunk.worldPosToChunkPos(playerPos);
        int updatedCount = 0;

        var minX = playerChunkPos.x - HORIZONTAL_LOAD_RADIUS;
        var maxX = playerChunkPos.x + HORIZONTAL_LOAD_RADIUS;
        var minY = playerChunkPos.y - VERTICAL_LOAD_RADIUS;
        var maxY = playerChunkPos.y + VERTICAL_LOAD_RADIUS;
        var minZ = playerChunkPos.z - HORIZONTAL_LOAD_RADIUS;
        var maxZ = playerChunkPos.z + HORIZONTAL_LOAD_RADIUS;

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
            var pos = chunk.getChunkGridPos();

            if (pos.x < minX || pos.x > maxX ||
                pos.y < minY || pos.y > maxY ||
                pos.z < minZ || pos.z > maxZ) {
                unloadChunk(chunk);
                if (hasAllUnloadedNeighbors(chunk)) {
                    it.remove();
                }
            } else {
                if (doUpdateChunk(chunk)) {
                    updatedCount++;
                }
            }

            // update any spoiled chunks that need updating
            // if they're being worked on, wait until they're ready (check again next frame)
            // if they've not had their blocks generated something's gone wrong but still we wait.
            if (chunk.spoiled
                    && !chunk.getStatus().working
                    && chunk.getStatus().urgency >= Chunk.Status.BLOCKS_GENERATED.urgency) {
                chunk.spoiled = false;
                var dst = playerChunkPos.sub(chunk.getChunkGridPos()).lengthSquared();

                if (dst < INSTANT_LOAD_DISTANCE_SQR) {
                    updateNow(chunk); // update on same frame on main thread
                } else {
                    forceChunkUpdate(chunk); // update multi-threaded on some future frame.
                }
            }
        }

        return updatedCount;
    }

    public static void updateNow(Chunk chunk) {
        // generate mesh
        chunk.setStatus(Chunk.Status.MESH_GENERATING);
        EntityManager.removeComponent(chunk, ChunkModelDataComponent.class);
        TerrainModelGenerator.loadChunk(chunk);

        chunk.setStatus(Chunk.Status.MESH_LOADING);
        var oldModel = EntityManager.removeComponent(chunk, ChunkModelComponent.class);
        EntityManager.removeComponent(chunk, TransformationComponent.class);
        TerrainModelLoader.loadChunk(chunk);

        if (oldModel != null) {
            oldModel.getModel().destroy();
        }

        assert chunk.getStatus() == Chunk.Status.FINAL;
    }

    /** Returns the size of the queue of all the chunk workers combined. */
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
        if (chunk == null || chunk.getStatus().urgency < Chunk.Status.BLOCKS_GENERATED.urgency) return;
        chunk.setBlockSafe(blockPos, block);
        chunk.spoiled = true;

        // setting a block at a chunk border requires the neighbor to update their mesh as well
        // TODO this might only apply with transparent blocks, maybe add a check for that?
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
        if (chunk == null || chunk.getStatus().urgency < Chunk.Status.BLOCKS_GENERATED.urgency) {
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

    private static void forceChunkUpdate(Chunk chunk) {
        // we assume this doesnt cause problems
        chunk.setStatus(Chunk.Status.BLOCKS_GENERATED);
        doUpdateChunk(chunk);
    }

    private static boolean doUpdateChunk(Chunk chunk) {
        switch (chunk.getStatus()) {
            // if chunk has no data, load heightmap terrain
            case NONE           -> {
                chunk.setStatus(Chunk.Status.BASIC_TERRAIN_GENERATING);
                TerrainGenerator.addChunks(chunk);
                return true;
            }
            // once chunk and all its neighbors have heightmapped terrain, load structures
            case BASIC_TERRAIN_GENERATED -> {
                if (neighborsUrgencyAtLeast(chunk, Chunk.Status.BASIC_TERRAIN_GENERATED.urgency)) {
                    chunk.setStatus(Chunk.Status.STRUCTURE_GENERATING);
                    StructureGenerator.addChunk(chunk);
                    return true;
                }
            }
            // once chunk has block data, generate block faces to form a mesh
            case BLOCKS_GENERATED -> {
                if (neighborsUrgencyAtLeast(chunk, Chunk.Status.BLOCKS_GENERATED.urgency)) {
                    chunk.setStatus(Chunk.Status.MESH_GENERATING);
                    TerrainModelGenerator.addChunk(chunk);
                    return true;
                }
            }
            // after mesh has been generated, load it into opengl.
            case MESH_GENERATED -> {
                chunk.setStatus(Chunk.Status.MESH_LOADING);
                TerrainModelLoader.addChunk(chunk);
                return true;
            }
        }

        return false;
    }

    /** Returns true if the chunk's neighbors have a status of at least `urgency`. */
    private static boolean neighborsUrgencyAtLeast(Chunk chunk, int urgency) {
        for (int dir = 0; dir < DiagonalDirection.COUNT; dir++) {
            var neighbor = chunk.getNeighbor(dir);
            if (neighbor == null || neighbor.getStatus().urgency < urgency) {
                return false;
            }
        }
        return true;
    }

    /** Returns true if all neighbors of this chunk have status NONE. */
    private static boolean hasAllUnloadedNeighbors(Chunk chunk) {
        for (int dir = 0; dir < DiagonalDirection.COUNT; dir++) {
            var neighbor = chunk.getNeighbor(dir);
            if (neighbor != null && neighbor.getStatus() != Chunk.Status.NONE) return false;
        }
        return true;
    }

    private static void unloadChunk(Chunk chunk) {
        if (chunk.getStatus().working) return; // don't want to unload a chunk that is queued somewhere.

        // remove components associated with this chunk
        var modelComp = EntityManager.removeComponent(chunk, ChunkModelComponent.class);
        EntityManager.removeComponent(chunk, ChunkModelDataComponent.class);
        EntityManager.removeComponent(chunk, TransformationComponent.class);

        // unload the model of the chunk
        if (modelComp != null) {
            modelComp.getModel().destroy();
        }

        // this just prevents log spam
        if (chunk.getStatus() != Chunk.Status.NONE) {
            chunk.setStatus(Chunk.Status.NONE);
        }
    }
}
