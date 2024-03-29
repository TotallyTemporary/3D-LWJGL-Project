package chunk;

import block.Block;
import block.DiagonalDirection;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.*;

/** This class stores and handles the loading of chunks.
 * It loads chunks by sending the chunks to various workers to be processed. */
public class ChunkLoader {

    // if these variables are to be user controlled, they should have 2-3 chunks added to them, to enable the player to always be in a fully loaded chunk.
    private static final int HORIZONTAL_LOAD_RADIUS = 12;
    private static final int VERTICAL_LOAD_RADIUS = 6;
    private static final int CHUNKS_COUNT = (2*HORIZONTAL_LOAD_RADIUS+1) * (2*VERTICAL_LOAD_RADIUS) * (2*HORIZONTAL_LOAD_RADIUS+1);

    // a chunk within this grid distance of the player's chunk will instantly get updated
    private static final float INSTANT_LOAD_DISTANCE = 2.5f;
    private static final float INSTANT_LOAD_DISTANCE_SQR = INSTANT_LOAD_DISTANCE*INSTANT_LOAD_DISTANCE;

    private static final int CHUNK_UPDATE_SLICE = 400; // only even try to update this many chunks in a frame

    private static final HashMap<Vector3i, Chunk> chunks = new LinkedHashMap<>(CHUNKS_COUNT);
    private static final Queue<Chunk> spoiledCloseQueue = new ConcurrentLinkedQueue<>();
    private static final Queue<Chunk> updateHintQueue = new ConcurrentLinkedQueue<>();

    private static Vector3i lastPlayerChunkPos = null;
    private static int slice = 0;

    // threading
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static CountDownLatch readyLatch = new CountDownLatch(0);

    // working buffers
    private static final ArrayList<Chunk> spoiledFarAway = new ArrayList<Chunk>();

    public static void start(Vector3f playerPos) {
        Vector3i playerChunkPos = Chunk.worldPosToChunkPos(playerPos);

        var minX = playerChunkPos.x - HORIZONTAL_LOAD_RADIUS;
        var maxX = playerChunkPos.x + HORIZONTAL_LOAD_RADIUS;
        var minY = playerChunkPos.y - VERTICAL_LOAD_RADIUS;
        var maxY = playerChunkPos.y + VERTICAL_LOAD_RADIUS;
        var minZ = playerChunkPos.z - HORIZONTAL_LOAD_RADIUS;
        var maxZ = playerChunkPos.z + HORIZONTAL_LOAD_RADIUS;

        for (int x = minX; x <= maxX; x++)
        for (int y = minY; y <= maxY; y++)
        for (int z = minZ; z <= maxZ; z++) {
            var pos = new Vector3i(x, y, z);
            var chunk = new Chunk(pos);
            chunks.put(pos, chunk);
        }

        lastPlayerChunkPos = playerChunkPos;
    }

    /** Unload all chunks */
    public static void stop() {
        executor.shutdown();
        while (chunks.size() > 0) {
            var it = chunks.values().iterator();
            while (it.hasNext()) {
                Chunk chunk = it.next();
                unloadChunk(chunk, true);
                if (chunk.getStatus() == Chunk.Status.NONE
                    && hasAllUnloadedNeighbors(chunk)) {
                    it.remove();
                }
            }
        }
    }

    public static void updatePriorityChunks() {
        while (!spoiledCloseQueue.isEmpty()) {
            Chunk chunk = spoiledCloseQueue.poll();
            updateNow(chunk);
        }
    }

    public static void hintChunkUpdateRequired(Chunk chunk) {
        updateHintQueue.add(chunk);
    }

    public static void startUpdate(Vector3f playerPos) {
        readyLatch = new CountDownLatch(1);
        executor.submit(() -> {
            try {
                update(playerPos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                readyLatch.countDown();
            }
        });
    }

    public static void endUpdate() {
        try {
            readyLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Updates chunks based on the player's position in the world.
     * @return number of chunks whose status was changed this frame. */
    public static int update(Vector3f playerPos) {
        // clear working arrays
        spoiledFarAway.clear();

        Vector3i playerChunkPos = Chunk.worldPosToChunkPos(playerPos);
        Vector3i playerDelta = playerChunkPos.sub(lastPlayerChunkPos, new Vector3i());

        // load new chunks and unload old chunks
        if (!playerDelta.equals(0, 0, 0)) {
            loadNewChunks(lastPlayerChunkPos, playerDelta);
            unloadChunks(lastPlayerChunkPos, playerDelta);
        }
        lastPlayerChunkPos = playerChunkPos;

        int updatedCount = 0;

        var minX = playerChunkPos.x - HORIZONTAL_LOAD_RADIUS;
        var maxX = playerChunkPos.x + HORIZONTAL_LOAD_RADIUS;
        var minY = playerChunkPos.y - VERTICAL_LOAD_RADIUS;
        var maxY = playerChunkPos.y + VERTICAL_LOAD_RADIUS;
        var minZ = playerChunkPos.z - HORIZONTAL_LOAD_RADIUS;
        var maxZ = playerChunkPos.z + HORIZONTAL_LOAD_RADIUS;

        var it = chunks.values().iterator();

        // skip forward to our slice
        int i = 0;
        while (i++ < slice && it.hasNext()) it.next();

        int chunkUpdated = 0;
        boolean hintQueueEmpty = false;
        while (
                (!hintQueueEmpty && !(hintQueueEmpty = updateHintQueue.isEmpty()))
                || it.hasNext()
        ) {
            Chunk chunk;
            // if the hint queue is empty, increment the count and update from iterator
            if (hintQueueEmpty) {
                if (chunkUpdated > CHUNK_UPDATE_SLICE) break;
                chunkUpdated += 1;
                chunk = it.next();
            } else {
                chunk = updateHintQueue.poll();
            }

            if (spoiledCloseQueue.contains(chunk)) {
                // don't update a chunk that will be updated on main thread later
                continue;
            }

            Vector3i chunkPos = chunk.getChunkGridPos();

            // update any spoiled chunks that need updating
            // if they're being worked on, wait until they're ready (check again next frame)
            // if they've not had their blocks generated something's gone wrong but still we wait.
            if (chunk.spoiled
                    && !chunk.getStatus().working
                    && chunk.getStatus().urgency >= Chunk.Status.BLOCKS_GENERATED.urgency) {
                chunk.spoiled = false;

                float dx = playerChunkPos.x - chunkPos.x;
                float dy = playerChunkPos.y - chunkPos.y;
                float dz = playerChunkPos.z - chunkPos.z;
                float dstSquared = dx*dx + dy*dy + dz*dz;
                if (dstSquared < INSTANT_LOAD_DISTANCE_SQR) {
                    spoiledCloseQueue.add(chunk);
                } else {
                    spoiledFarAway.add(chunk);
                }
                continue;
            }

            if (chunkPos.x < minX || chunkPos.x > maxX
             || chunkPos.y < minY || chunkPos.y > maxY
             || chunkPos.z < minZ || chunkPos.z > maxZ) {
                unloadChunk(chunk, true);
                if (chunk.getStatus() == Chunk.Status.NONE && hasAllUnloadedNeighbors(chunk)) {
                    it.remove();
                }
            } else {
                if (doUpdateChunk(chunk)) {
                    updatedCount++;
                }
            }
        }

        // update slice variable
        slice += chunkUpdated;
        if (slice >= chunks.size()) {
            slice = 0;
        }

        // update far away spoiled after the close spoiled have been updated
        for (Chunk chunk : spoiledFarAway) {
            chunk.setStatus(Chunk.Status.BLOCKS_GENERATED); // update multi-threaded on some future frame.
        }

        // skip lightmap generation for top chunks
        int topChunkLevel = maxY - 2;
        for (var chunk : chunks.values()) {
            if (chunk.getChunkGridPos().y == topChunkLevel
                && chunk.getStatus() == Chunk.Status.BLOCKS_GENERATED
                && !chunk.getIsAirChunk()) {
                chunk.setStatus(Chunk.Status.LIGHTS_GENERATED);
            }
        }

        return updatedCount;
    }

    public static void updateNow(Chunk chunk) {
        if (chunk.getStatus().working) return;

        // unload current model
        unloadChunkNow(chunk, false);

        // update lightmap
        chunk.setStatus(Chunk.Status.LIGHTS_GENERATING);
        LightMapGenerator.loadChunk(chunk);

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
                + TerrainModelLoader.getQueueSize()
                + LightMapGenerator.getQueueSize()
                + ChunkSerializer.getQueueSize();
    }

    public static void setBlockAt(Vector3i pos, byte block) {
        var chunkPos = Chunk.worldPosToChunkPos(pos);
        var blockPos = Chunk.worldPosToBlockPos(pos);
        var chunk = chunks.get(chunkPos);
        if (chunk == null || chunk.getStatus().urgency < Chunk.Status.BLOCKS_GENERATED.urgency) return;
        chunk.setBlockSafe(blockPos, block);
        chunk.spoil();

        // setting a block at a chunk border requires the neighbor to update their mesh as well
        // TODO this might only apply with transparent blocks, maybe add a check for that?
        if (blockPos.x == 0)            chunk.getNeighbor(DiagonalDirection.LEFT).spoil();
        if (blockPos.x == Chunk.SIZE-1) chunk.getNeighbor(DiagonalDirection.RIGHT).spoil();
        if (blockPos.y == 0)            chunk.getNeighbor(DiagonalDirection.DOWN).spoil();
        if (blockPos.y == Chunk.SIZE-1) chunk.getNeighbor(DiagonalDirection.UP).spoil();
        if (blockPos.z == 0)            chunk.getNeighbor(DiagonalDirection.FRONT).spoil();
        if (blockPos.z == Chunk.SIZE-1) chunk.getNeighbor(DiagonalDirection.BACK).spoil();
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

    private static void loadNewChunks(Vector3i lastPos, Vector3i playerMovedDelta) {
        final int horiz = HORIZONTAL_LOAD_RADIUS;
        final int vert = VERTICAL_LOAD_RADIUS;

        // pretend the player hasn't moved, we move it axis-by-axis
        Vector3i player = new Vector3i(lastPos);
        Vector3i playerDelta = new Vector3i(playerMovedDelta);

        // +x
        while (playerDelta.x > 0) {
            for (int y = player.y - vert; y <= player.y + vert; y++)
            for (int z = player.z - horiz; z <= player.z + horiz; z++)
            {
                ensureChunkLoaded(player.x + horiz + 1, y, z);
            }
            playerDelta.x -= 1;
            player.x += 1;
        }

        // -x
        while (playerDelta.x < 0) {
            for (int y = player.y - vert; y <= player.y + vert; y++)
            for (int z = player.z - horiz; z <= player.z + horiz; z++)
            {
                ensureChunkLoaded(player.x - horiz - 1, y, z);
            }
            playerDelta.x += 1;
            player.x -= 1;
        }

        // +y
        while (playerDelta.y > 0) {
            for (int x = player.x - horiz; x <= player.x + horiz; x++)
            for (int z = player.z - horiz; z <= player.z + horiz; z++)
            {
                ensureChunkLoaded(x, player.y + vert + 1, z);
            }
            playerDelta.y -= 1;
            player.y += 1;
        }

        // -y
        while (playerDelta.y < 0) {
            for (int x = player.x - horiz; x <= player.x + horiz; x++)
            for (int z = player.z - horiz; z <= player.z + horiz; z++)
            {
                ensureChunkLoaded(x, player.y - vert - 1, z);
            }
            playerDelta.y += 1;
            player.y -= 1;
        }

        // +z
        while (playerDelta.z > 0) {
            for (int y = player.y - vert; y <= player.y + vert; y++)
            for (int x = player.x - horiz; x <= player.x + horiz; x++)
            {
                ensureChunkLoaded(x, y, player.z + horiz + 1);
            }
            playerDelta.z -= 1;
            player.z += 1;
        }

        // -z
        while (playerDelta.z < 0) {
            for (int y = player.y - vert; y <= player.y + vert; y++)
            for (int x = player.x - horiz; x <= player.x + horiz; x++)
            {
                ensureChunkLoaded(x, y, player.z - horiz - 1);
            }
            playerDelta.z += 1;
            player.z -= 1;
        }
    }

    private static void unloadChunks(Vector3i lastPos, Vector3i playerMovedDelta) {
        final int horiz = HORIZONTAL_LOAD_RADIUS;
        final int vert = VERTICAL_LOAD_RADIUS;

        // pretend the player hasn't moved, we move it axis-by-axis
        Vector3i player = new Vector3i(lastPos);
        Vector3i playerDelta = new Vector3i(playerMovedDelta);

        // +x
        while (playerDelta.x > 0) {
            for (int y = player.y - vert; y <= player.y + vert; y++)
            for (int z = player.z - horiz; z <= player.z + horiz; z++)
            {
                unloadChunkAt(player.x - horiz, y, z);
            }
            playerDelta.x -= 1;
            player.x += 1;
        }

        // -x
        while (playerDelta.x < 0) {
            for (int y = player.y - vert; y <= player.y + vert; y++)
            for (int z = player.z - horiz; z <= player.z + horiz; z++)
            {
                unloadChunkAt(player.x + horiz, y, z);
            }
            playerDelta.x += 1;
            player.x -= 1;
        }

        // +y
        while (playerDelta.y > 0) {
            for (int x = player.x - horiz; x <= player.x + horiz; x++)
            for (int z = player.z - horiz; z <= player.z + horiz; z++)
            {
                unloadChunkAt(x, player.y - vert, z);
            }
            playerDelta.y -= 1;
            player.y += 1;
        }

        // -y
        while (playerDelta.y < 0) {
            for (int x = player.x - horiz; x <= player.x + horiz; x++)
            for (int z = player.z - horiz; z <= player.z + horiz; z++)
            {
                unloadChunkAt(x, player.y + vert, z);
            }
            playerDelta.y += 1;
            player.y -= 1;
        }

        // +z
        while (playerDelta.z > 0) {
            for (int y = player.y - vert; y <= player.y + vert; y++)
            for (int x = player.x - horiz; x <= player.x + horiz; x++)
            {
                unloadChunkAt(x, y, player.z - horiz);
            }
            playerDelta.z -= 1;
            player.z += 1;
        }

        // -z
        while (playerDelta.z < 0) {
            for (int y = player.y - vert; y <= player.y + vert; y++)
            for (int x = player.x - horiz; x <= player.x + horiz; x++)
            {
                unloadChunkAt(x, y, player.z + horiz);
            }
            playerDelta.z += 1;
            player.z -= 1;
        }
    }

    private static boolean ensureChunkLoaded(int x, int y, int z) {
        Vector3i pos = new Vector3i(x, y, z);
        if (!chunks.containsKey(pos)) {
            var chunk = new Chunk(pos);
            chunks.put(pos, chunk);
            return true;
        }
        return false;
    }

    private static boolean unloadChunkAt(int x, int y, int z) {
        Vector3i pos = new Vector3i(x, y, z);
        if (chunks.containsKey(pos)) {
            var chunk = chunks.get(pos);
            unloadChunk(chunk, true);
            return true;
        }
        return false;
    }

    private static boolean doUpdateChunk(Chunk chunk) {
        switch (chunk.getStatus()) {
            // if chunk has no data, load heightmap terrain
            case NONE           -> {
                if (ChunkSerializer.doesSaveExist(chunk)) {
                    chunk.setStatus(Chunk.Status.DESERIALIZING);
                    ChunkSerializer.deserialize(chunk);
                    return true;
                } else {
                    chunk.setStatus(Chunk.Status.BASIC_TERRAIN_GENERATING);
                    TerrainGenerator.addChunks(chunk);
                    return true;
                }
            }
            // once chunk and all its neighbors have heightmapped terrain, load structures
            case BASIC_TERRAIN_GENERATED -> {
                if (neighborsUrgencyAtLeast(chunk, Chunk.Status.BASIC_TERRAIN_GENERATED.urgency)) {
                    chunk.setStatus(Chunk.Status.STRUCTURE_GENERATING);
                    StructureGenerator.addChunk(chunk);
                    return true;
                }
            }
            case BLOCKS_GENERATED -> {
                // if not air chunk and can't normally generate lights, then the chunk is skipped in lightmap generation
                if (!chunk.getIsAirChunk()) {
                    boolean canNormallyGenerate = neighborsUrgencyAtLeast(chunk, Chunk.Status.BLOCKS_GENERATED.urgency)
                            && aboveNeighborsUrgencyAtLeast(chunk, Chunk.Status.LIGHTS_GENERATED.urgency);
                    if (!canNormallyGenerate) {
                        return false;
                    }
                }

                chunk.setStatus(Chunk.Status.LIGHTS_GENERATING);
                LightMapGenerator.addChunk(chunk);
                return true;
            }

            // once chunk has block and light data, generate block faces to form a mesh
            case LIGHTS_GENERATED -> {
                if (neighborsUrgencyAtLeast(chunk, Chunk.Status.LIGHTS_GENERATED.urgency)) {
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

    private static boolean aboveNeighborsUrgencyAtLeast(Chunk chunk, int urgency) {
        for (int dir : DiagonalDirection.ABOVE) {
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

    private static void unloadChunk(Chunk chunk, boolean serialize) {
        if (chunk.getStatus().working) return; // don't want to unload a chunk that is queued somewhere.

        boolean hasBlocks = chunk.getStatus().urgency > Chunk.Status.BLOCKS_GENERATED.urgency;

        EntityManager.removeEntitySafe(chunk);
        chunk.setStatus(Chunk.Status.NONE);

        if (serialize && hasBlocks) {
            chunk.setStatus(Chunk.Status.SERIALIZING);
            ChunkSerializer.serialize(chunk);
        }
    }

    private static void unloadChunkNow(Chunk chunk, boolean serialize) {
        if (chunk.getStatus().working) return;

        boolean hasBlocks = chunk.getStatus().urgency > Chunk.Status.BLOCKS_GENERATED.urgency;

        EntityManager.removeEntity(chunk);
        chunk.setStatus(Chunk.Status.NONE);

        if (serialize && hasBlocks) {
            chunk.setStatus(Chunk.Status.SERIALIZING);
            ChunkSerializer.serialize(chunk);
        }
    }
}
