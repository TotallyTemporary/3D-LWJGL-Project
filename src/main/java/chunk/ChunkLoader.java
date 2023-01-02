package chunk;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

public class ChunkLoader {

    private static final int LOAD_RADIUS = 3;

    private static HashMap<Vector3i, Chunk> chunks = new HashMap<>();
    private static Queue<Chunk> modelLoadQueue = new ArrayDeque<>();

    public static int update(Vector3f playerPos) {
        Vector3i playerChunkPos = Chunk.worldPosToChunkPos(playerPos);

        int updatedCount = 0;
        for (int x = playerChunkPos.x - LOAD_RADIUS; x < playerChunkPos.x + LOAD_RADIUS +1; x++)
        for (int y = playerChunkPos.y - LOAD_RADIUS; y < playerChunkPos.y + LOAD_RADIUS +1; y++)
        for (int z = playerChunkPos.z - LOAD_RADIUS; z < playerChunkPos.z + LOAD_RADIUS +1; z++)
        {
            var pos = new Vector3i(x, y, z);
            var chunk = chunks.get(pos);
            if (chunk == null) {
                chunk = new Chunk(pos);
                chunks.put(pos, chunk);
            }

            switch (chunk.getStatus()) {
                case NONE           -> {
                    TerrainGenerator.addChunk(chunk);
                    updatedCount++;
                }
                case WAIT_NEIGHBORS -> {
                    if (canGenerateModel(pos)) {
                        TerrainModelGenerator.addChunk(chunk);
                        updatedCount++;
                    }
                }
                case PREPARED -> {
                    TerrainModelLoader.addChunk(chunk);
                    updatedCount++;
                }
            }
        }

        return updatedCount;
    }

    private static boolean canGenerateModel(Vector3i pos) {
        for (var neighborPos : neighbors(pos)) {
            var neighbor = chunks.get(neighborPos);
            if (neighbor == null || neighbor.getStatus().urgency < Chunk.Status.WAIT_NEIGHBORS.urgency) {
                return false;
            }
        }
        return true;
    }

    private static Vector3i[] neighbors(Vector3i pos) {
        return new Vector3i[]{
            new Vector3i(pos.x+1, pos.y, pos.z),
            new Vector3i(pos.x-1, pos.y, pos.z),
            new Vector3i(pos.x, pos.y+1, pos.z),
            new Vector3i(pos.x, pos.y-1, pos.z),
            new Vector3i(pos.x, pos.y, pos.z+1),
            new Vector3i(pos.x, pos.y, pos.z-1)
        };
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

}
