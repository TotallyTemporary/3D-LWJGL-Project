package chunk;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;

public class ChunkLoader {

    private static final int LOAD_RADIUS = 4;

    private static HashMap<Vector3i, Chunk> chunks = new HashMap<>();

    public static int update(Vector3f playerPos) {
        Vector3i playerChunkPos = Chunk.worldPosToChunkPos(playerPos);

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

        return updatedCount;
    }

    private static boolean doUpdateChunk(int x, int y, int z) {
        var pos = new Vector3i(x, y, z);
        var chunk = chunks.get(pos);
        if (chunk == null) {
            chunk = new Chunk(pos);
            chunks.put(pos, chunk);
        }

        switch (chunk.getStatus()) {
            case NONE           -> {
                TerrainGenerator.addChunk(chunk);
                return true;
            }
            case WAIT_NEIGHBORS -> {
                if (canGenerateModel(chunk)) {
                    TerrainModelGenerator.addChunk(chunk);
                    return true;
                }
            }
            case PREPARED -> {
                TerrainModelLoader.addChunk(chunk);
                return true;
            }
        }

        return false;
    }

    private static boolean canGenerateModel(Chunk chunk) {
        for (int dir = 0; dir < 6; dir++) {
            var neighbor = chunk.getNeighbor(dir);
            if (neighbor == null || neighbor.getStatus().urgency < Chunk.Status.WAIT_NEIGHBORS.urgency) {
                return false;
            }
        }
        return true;
    }

    public static byte getBlockAt(Vector3f pos) {
        var chunkPos = Chunk.worldPosToChunkPos(pos);
        var chunkRemainderPos = Chunk.worldPosToBlockPos(pos);

        var chunk = chunks.get(chunkPos);
        if (chunk == null) return Block.INVALID.getID();
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
