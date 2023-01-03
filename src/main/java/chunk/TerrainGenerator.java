package chunk;

import org.joml.Vector3i;

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
        byte[] blocks = new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE];

        var heightMap = new float[Chunk.SIZE][Chunk.SIZE];
        for (int x = 0; x < Chunk.SIZE; x++)
        for (int z = 0; z < Chunk.SIZE; z++) {
            heightMap[x][z] = 1000 * (int) Math.sin(x + z);
        }

        for (int chunkX = 0; chunkX < Chunk.SIZE; chunkX++)
        for (int chunkY = 0; chunkY < Chunk.SIZE; chunkY++)
        for (int chunkZ = 0; chunkZ < Chunk.SIZE; chunkZ++)
        {
            var index = Chunk.toIndex(chunkX, chunkY, chunkZ);
            var worldPos = Chunk.blockPosToWorldPos(new Vector3i(chunkX, chunkY, chunkZ), chunk);


            var block = Block.AIR.getID();
            if (worldPos.y <= heightMap[chunkX][chunkZ]) {
                block = Block.STONE.getID();
            }

            blocks[index] = block;
        }

        blocks[0] = Block.STONE.getID();
        blocks[1] = Block.STONE.getID();

        chunk.setBlocks(blocks);
    }
}
