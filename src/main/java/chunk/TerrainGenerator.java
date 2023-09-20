package chunk;

import biome.Biome;
import biomes.Biomes;
import block.Block;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.modules.octavation.OctavationModule;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import entity.EntityManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Random;
import java.util.concurrent.*;

/** Makes terrain via simplex noise. The terrain consists of a heightmap, and caves are carved with a simple noise check. */
public class TerrainGenerator {

    private static final int TERRAIN_SEED = 1235;

    private static final int WATER_LEVEL = 75;

    private static final Object noiseLock = new Object();

    private static final Vector3f CAVE_SCALE = new Vector3f(0.025f, 0.035f, 0.025f);
    private static final float CAVE_CUTOFF = 0.70f;

    private static final float RIVER_SCALE = 1f / 1_000;
    private static final float RIVER_WARP_SCALE = 0.005f;
    private static final float RIVER_WARP_INTENSITY = 0.009f;
    private static final float RIVER_SCALE_SCALE = 1f / 5_000; // scale for the noise that determines river scale (between smallest and largest river)

    // smallest river
    private static final float SMALL_RIVER_BOTTOM_SIZE = 0.20f, // what percentage of the graph should be full river bottom
                               SMALL_RIVER_STEEPNESS = 0.90f,
                               SMALL_RIVER_LEVEL = WATER_LEVEL - 3,
                               SMALL_RIVER_CUTOFF = 0.04f;

    // largest river
    private static final float LARGE_RIVER_BOTTOM_SIZE = 0.10f,
                               LARGE_RIVER_STEEPNESS = 0.80f,
                               LARGE_RIVER_LEVEL = WATER_LEVEL - 10,
                               LARGE_RIVER_CUTOFF = 0.08f;

    private static final float BIOME_SMOOTHING = 1.5f; // 1f=biomes "pull" each other really far away, 2f=biome changes too drastic
    private static final float BIOME_SCALE = 0.0005f;

    private static final float BIOME_WARP_SCALE = 0.04f;
    private static final float BIOME_WARP_INTENSITY = 0.007f;

    private static final float LOWEST_OCTAVE_SCALE = 0.008f;
    private static final int OCTAVES = 3;

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
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

    private static void generateHeightAndBiomemap(Chunk chunk, int[][] heightMap, Biome[][] biomeMap) {
        // calculate biomemap and heightmap
        NoiseSource
                heightMapRandom,
                riverRandom, riverWarpXRandom, riverWarpZRandom, riverScaleRandom,
                humidityRandom, temperatureRandom, biomeWarpX, biomeWarpZ;
        synchronized (noiseLock) {
            heightMapRandom = FastSimplexNoiseGenerator.newBuilder().setSeed(TERRAIN_SEED ^ 90485924).build();
            riverRandom = FastSimplexNoiseGenerator.newBuilder().setSeed(TERRAIN_SEED ^ 189234).build();
            riverWarpXRandom = FastSimplexNoiseGenerator.newBuilder().setSeed(TERRAIN_SEED ^ 92838059).build();
            riverWarpZRandom = FastSimplexNoiseGenerator.newBuilder().setSeed(TERRAIN_SEED ^ 918340).build();
            riverScaleRandom = FastSimplexNoiseGenerator.newBuilder().setSeed(TERRAIN_SEED ^ 1928387).build();
            humidityRandom = FastSimplexNoiseGenerator.newBuilder().setSeed(TERRAIN_SEED ^ 4909054).build();
            temperatureRandom = FastSimplexNoiseGenerator.newBuilder().setSeed(TERRAIN_SEED ^ 1345090).build();
            biomeWarpX = FastSimplexNoiseGenerator.newBuilder().setSeed(TERRAIN_SEED ^ 49115345).build();
            biomeWarpZ = FastSimplexNoiseGenerator.newBuilder().setSeed(TERRAIN_SEED ^ 29830913).build();
        }

        for (int chunkX = 0; chunkX < Chunk.SIZE; chunkX++)
        for (int chunkZ = 0; chunkZ < Chunk.SIZE; chunkZ++)
        {
            var worldPos = Chunk.blockPosToWorldPos(new Vector3i(chunkX, 0, chunkZ), chunk);

            float warpX = BIOME_WARP_INTENSITY * (float) biomeWarpX.evaluateNoise(worldPos.x * BIOME_WARP_SCALE, worldPos.z * BIOME_WARP_SCALE);
            float warpZ = BIOME_WARP_INTENSITY * (float) biomeWarpZ.evaluateNoise(worldPos.x * BIOME_WARP_SCALE, worldPos.z * BIOME_WARP_SCALE);

            float humidity = (float) humidityRandom.evaluateNoise(warpX + worldPos.x * BIOME_SCALE,
                                                                  warpZ + worldPos.z * BIOME_SCALE);
            float temperature = (float) temperatureRandom.evaluateNoise(warpX + worldPos.x * BIOME_SCALE,
                                                                        warpZ + worldPos.z * BIOME_SCALE);
            humidity = (humidity + 1) / 2f;
            temperature = (temperature + 1) / 2f;

            // humidity *= temperature; // if temperature is really low, the humidity cannot get high
            humidity = Math.min(humidity, temperature); // doesn't skew our distribution toward (0,0)

            float baseline = 0f;
            float amplitude = 0f;
            float roughness = 0f;
            float sumWeights = 0f;

            Biome closestBiome = null;
            float minDistance = Float.MAX_VALUE;

            for (var entry : Biomes.biomes.entrySet()) {
                Vector2f vec = entry.getKey();
                Biome biome = entry.getValue();

                float dh = Math.abs(humidity - vec.x);
                float dt = Math.abs(temperature - vec.y);
                float dst = dh*dh + dt*dt;

                // inverse distance weighting
                float weight = 1f / (float) Math.pow(dst, BIOME_SMOOTHING);
                baseline += biome.getBaselineHeight() * weight;
                amplitude += biome.getAmplitude() * weight;
                roughness += biome.getRoughness() * weight;
                sumWeights += weight;

                // get closest biome
                if (dst < minDistance) {
                    minDistance = dst;
                    closestBiome = biome;
                }
            }

            baseline /= sumWeights;
            amplitude /= sumWeights;
            roughness /= sumWeights;
            biomeMap[chunkX][chunkZ] = closestBiome;

            float sumHeight = 0f;

            float octaveFrequency = LOWEST_OCTAVE_SCALE;
            float octaveAmplitude = amplitude;
            for (int i = 0; i < OCTAVES; i++) {
                float noise = (float) heightMapRandom.evaluateNoise(worldPos.x * octaveFrequency, worldPos.z * octaveFrequency);
                sumHeight += octaveAmplitude * noise;

                octaveFrequency *= 3f;
                octaveAmplitude *= roughness;
            }

            float height = baseline + sumHeight;

            // get river scale here
            float riverScale = (float) riverScaleRandom.evaluateNoise(worldPos.x * RIVER_SCALE_SCALE, worldPos.z * RIVER_SCALE_SCALE);
            riverScale = (riverScale + 1) / 2f; // between 0 and 1

            // interpolate between small and large river
            float riverBottomSize = (1 - riverScale) * SMALL_RIVER_BOTTOM_SIZE + riverScale * LARGE_RIVER_BOTTOM_SIZE;
            float riverSteepness  = (1 - riverScale) * SMALL_RIVER_STEEPNESS + riverScale * LARGE_RIVER_STEEPNESS;
            float riverLevel = (1 - riverScale) * SMALL_RIVER_LEVEL + riverScale * LARGE_RIVER_LEVEL;
            float riverCutoff = (1 - riverScale) * SMALL_RIVER_CUTOFF + riverScale * LARGE_RIVER_CUTOFF;

            float riverWarpX = RIVER_WARP_INTENSITY * (float) riverWarpXRandom.evaluateNoise(worldPos.x * RIVER_WARP_SCALE, worldPos.z * RIVER_WARP_SCALE);
            float riverWarpZ = RIVER_WARP_INTENSITY * (float) riverWarpZRandom.evaluateNoise(worldPos.x * RIVER_WARP_SCALE, worldPos.z * RIVER_WARP_SCALE);
            float riverNoise = (float) riverRandom.evaluateNoise(
                    riverWarpX + worldPos.x * RIVER_SCALE,
                    riverWarpZ + worldPos.z * RIVER_SCALE);
            riverNoise = Math.abs(riverNoise); // ridge noise
            riverNoise = Math.min(riverNoise, riverCutoff) / riverCutoff; // only take peaks
            riverNoise = 1 - riverNoise; // invert
            riverNoise = Math.min(riverNoise * (1 + riverBottomSize), 1); // give river a smooth bottom
            riverNoise = (float) Math.pow(riverNoise, riverSteepness); // make river cut less linear
            height = height * (1 - riverNoise) + riverLevel * riverNoise; // mix between river level and normal height

            heightMap[chunkX][chunkZ] = (int) height;
        }
    }

    private static boolean isCave(Vector3i pos, NoiseSource generator) {
        float noise = (float) generator.evaluateNoise(
                                    pos.x * CAVE_SCALE.x,
                                    pos.y * CAVE_SCALE.y,
                                    pos.z * CAVE_SCALE.z);
        noise = (1 + noise) / 2f;
        boolean isCave = noise < CAVE_CUTOFF;
        return isCave;
    }

    private static void generateFirstpass(Chunk chunk) {
        // generate blocks
        byte[] blocks = new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE];

        int[][] heightMap = new int[Chunk.SIZE][Chunk.SIZE];
        Biome[][] biomeMap = new Biome[Chunk.SIZE][Chunk.SIZE];
        generateHeightAndBiomemap(chunk, heightMap, biomeMap);

        boolean isAirChunk = true;

        NoiseSource caveGenerator;
        synchronized (noiseLock) {
            var caveSimplex = FastSimplexNoiseGenerator
                .newBuilder()
                .setSeed(TERRAIN_SEED)
                .build();
            caveGenerator = OctavationModule
                    .newBuilder()
                    .setNoiseSource(caveSimplex)
                    .setFractalFunction(FractalFunction.RIDGED_MULTI)
                    .build();
        }


        var biomeRandom = new Random();
        for (int chunkX = 0; chunkX < Chunk.SIZE; chunkX++)
        for (int chunkY = 0; chunkY < Chunk.SIZE; chunkY++)
        for (int chunkZ = 0; chunkZ < Chunk.SIZE; chunkZ++)
        {
            int index = Chunk.toIndex(chunkX, chunkY, chunkZ);
            Vector3i worldPos = Chunk.blockPosToWorldPos(new Vector3i(chunkX, chunkY, chunkZ), chunk);

            int terrainLevel = heightMap[chunkX][chunkZ];
            Biome biome = biomeMap[chunkX][chunkZ];
            int depth = terrainLevel - worldPos.y;

            byte block;
            biomeRandom.setSeed(TERRAIN_SEED ^ (worldPos.x * 132904L + worldPos.y * 12205L * worldPos.z * 390408L));

            if (depth < 0) {
                if (worldPos.y < WATER_LEVEL) {
                    block = Block.WATER.getID();
                } else {
                    block = Block.AIR.getID();
                }
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

        var biomeMapComp = new TerrainMapDataComponent(heightMap, biomeMap); // required for structure generation
        EntityManager.addComponent(chunk, biomeMapComp);
    }
}
