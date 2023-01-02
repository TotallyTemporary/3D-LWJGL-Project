package chunk;

import java.util.*;

public class TerrainGenerator {

    private static final Queue<Chunk> chunkGenQueue = new ArrayDeque<>();

    public static void loadChunks() {
        Chunk chunk;
        while ((chunk = chunkGenQueue.poll()) != null) {
            generateTerrain(chunk);
            chunk.setStatus(Chunk.Status.WAIT_NEIGHBORS);
        }
    }

    public static void addChunk(Chunk chunk) {
        chunk.setStatus(Chunk.Status.TERRAIN_GENERATING);
        chunkGenQueue.add(chunk);
    }

    private static void generateTerrain(Chunk chunk) {
        // generate blocks
        byte[][][] blocks = new byte[Chunk.SIZE][Chunk.SIZE][Chunk.SIZE];

        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++)
        {
            blocks[x][y][z] = Block.AIR.getID();
        }

        blocks[5][5][5] = Block.STONE.getID();
        blocks[5][6][5] = Block.STONE.getID();

        chunk.setBlocks(blocks);
    }
}
