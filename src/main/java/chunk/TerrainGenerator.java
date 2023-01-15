package chunk;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;
import org.joml.Vector3i;

import java.util.concurrent.*;

public class TerrainGenerator {

    private static final int WORLD_SEED = 1235;
    private static final float SCALE = 0.01f;
    private static final float AMPLITUDE = 35f;
    private static final float MIN_HEIGHT = 10f;

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    public static void addChunks(Chunk chunk) {
        executor.submit(() -> loadChunk(chunk));
    }

    public static void loadChunk(Chunk chunk) {
        generateFirstpass(chunk);
        chunk.setStatus(Chunk.Status.WAIT_NEIGHBORS);
    }

    public static void stop() {
        executor.shutdownNow();
    }

    private static void generateFirstpass(Chunk chunk) {
        // generate blocks
        byte[] blocks = new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE];
        boolean isAllAir = true;

        // calc heightmap
        int[][] heightMap = new int[Chunk.SIZE][Chunk.SIZE];
        var heightMapRandom = getNoiseGenerator();

        for (int chunkX = 0; chunkX < Chunk.SIZE; chunkX++)
        for (int chunkZ = 0; chunkZ < Chunk.SIZE; chunkZ++)
        {
            var worldPos = Chunk.blockPosToWorldPos(new Vector3i(chunkX, 0, chunkZ), chunk);
            heightMap[chunkX][chunkZ] = (int) (MIN_HEIGHT + AMPLITUDE *
                            (1 + heightMapRandom.evaluateNoise(worldPos.x * SCALE, worldPos.z * SCALE)) / 2f);
        }



        for (int chunkX = 0; chunkX < Chunk.SIZE; chunkX++)
        for (int chunkY = 0; chunkY < Chunk.SIZE; chunkY++)
        for (int chunkZ = 0; chunkZ < Chunk.SIZE; chunkZ++)
        {
            var index = Chunk.toIndex(chunkX, chunkY, chunkZ);
            var worldPos = Chunk.blockPosToWorldPos(new Vector3i(chunkX, chunkY, chunkZ), chunk);
            var terrainLevel = heightMap[chunkX][chunkZ];

            byte block;
            if (worldPos.y == terrainLevel) {
                block = Block.GRASS.getID();
            } else if (terrainLevel-3 < worldPos.y && worldPos.y < terrainLevel) {
                block = Block.DIRT.getID();
            } else if (0 < worldPos.y && worldPos.y <= terrainLevel-3){
                block = Block.STONE.getID();
            } else if (worldPos.y == 0) {
                block = Block.BEDROCK.getID();
            } else {
                block = Block.AIR.getID();
            }

            if (block != Block.AIR.getID()) isAllAir = false;
            blocks[index] = block;
        }
        if (isAllAir) chunk.isAllAir();
        else chunk.setBlocks(blocks);
    }

    private static synchronized FastSimplexNoiseGenerator getNoiseGenerator() {
        return FastSimplexNoiseGenerator.newBuilder()
                .setSeed(WORLD_SEED).build();
    }
}
