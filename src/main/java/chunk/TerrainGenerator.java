package chunk;

import biome.Biome;
import biomes.ForestBiome;
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import org.joml.Vector3i;

import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** Makes terrain via simplex noise. The terrain consists of a heightmap, and caves are carved with a simple noise check. */
public class TerrainGenerator {

    private static final int TERRAIN_SEED = 1235;

    private static final float CAVE_SCALE = 0.03f,
                               CAVE_CUTOFF = 0.2f;

    private static final Biome biome = new ForestBiome();

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    static { System.out.println("TerrainGenerator running"); }

    public static void addChunks(Chunk chunk) {
        executor.submit(() -> loadChunk(chunk));
    }

    public static void loadChunk(Chunk chunk) {
        generateFirstpass(chunk);
        chunk.setStatus(Chunk.Status.BASIC_TERRAIN_GENERATED);
    }

    public static int getQueueSize() {
        return executor.getQueue().size();
    }

    public static void stop() {
        executor.shutdownNow();
        System.out.println("TerrainGenerator stopped");
    }

    private static int[][] generateHeightmap(Chunk chunk) {
        // calc heightmap
        int[][] heightMap = new int[Chunk.SIZE][Chunk.SIZE];
        var heightMapRandom = getNoiseGenerator();

        for (int chunkX = 0; chunkX < Chunk.SIZE; chunkX++)
        for (int chunkZ = 0; chunkZ < Chunk.SIZE; chunkZ++)
        {
            var worldPos = Chunk.blockPosToWorldPos(new Vector3i(chunkX, 0, chunkZ), chunk);

            float scale = biome.getScale();
            float noise = (float) heightMapRandom.evaluateNoise(worldPos.x * scale, worldPos.z * scale);
            float height = biome.getBaselineHeight() + biome.getAmplitude() * noise;
            heightMap[chunkX][chunkZ] = (int) height;
        }

        return heightMap;
    }

    private static boolean isCave(Vector3i pos, FastSimplexNoiseGenerator generator) {
        float noise = (1 + (float) generator.evaluateNoise(
                                    pos.x * CAVE_SCALE,
                                    pos.y * CAVE_SCALE,
                                    pos.z * CAVE_SCALE)) / 2f;

        return noise < CAVE_CUTOFF;
    }

    private static void generateFirstpass(Chunk chunk) {
        // generate blocks
        byte[] blocks = new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE];

        int[][] heightMap = generateHeightmap(chunk);
        boolean isAirChunk = true;

        var caveGenerator = getNoiseGenerator();
        var biomeRandom = new Random();
        for (int chunkX = 0; chunkX < Chunk.SIZE; chunkX++)
        for (int chunkY = 0; chunkY < Chunk.SIZE; chunkY++)
        for (int chunkZ = 0; chunkZ < Chunk.SIZE; chunkZ++)
        {
            int index = Chunk.toIndex(chunkX, chunkY, chunkZ);
            Vector3i worldPos = Chunk.blockPosToWorldPos(new Vector3i(chunkX, chunkY, chunkZ), chunk);
            int terrainLevel = heightMap[chunkX][chunkZ];
            int depth = terrainLevel - worldPos.y;

            byte block;
            biomeRandom.setSeed(TERRAIN_SEED ^ (worldPos.x * 132904L + worldPos.y * 12205L * worldPos.z * 390408L));

            if (depth < 0) {
                block = Block.AIR.getID();
            } else {
                if (isCave(worldPos, caveGenerator)) {
                    block = Block.AIR.getID();
                } else {
                    block = biome.getBlock(depth, biomeRandom::nextFloat).getID();
                }
            }

            if (block != Block.AIR.getID()) isAirChunk = false;
            blocks[index] = block;
        }
        chunk.setBlocks(blocks);
        chunk.setColours(new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE]);
        chunk.setIsAirChunk(isAirChunk);
    }

    private static synchronized FastSimplexNoiseGenerator getNoiseGenerator() {
        return FastSimplexNoiseGenerator.newBuilder()
                .setSeed(TERRAIN_SEED).build();
    }

    private static float oreChance(float depth, float deep, float deepChance, float high, float highChance) {
        if (depth < deep) return deepChance;
        if (depth > high) return highChance;
        float ratio = (depth - deep) / (high - deep);
        return highChance + smoothstep(1f - ratio) * deepChance;
    }

    private static float smoothstep(float x) {
        return (float) (6*Math.pow(x, 5) - 15*Math.pow(x, 4) + 10*Math.pow(x, 3));
    }
}
