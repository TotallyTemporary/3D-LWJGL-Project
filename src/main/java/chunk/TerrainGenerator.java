package chunk;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import org.joml.Vector3i;

import java.util.concurrent.*;

/** Makes terrain via simplex noise. The terrain consists of a heightmap, and caves are carved with a simple noise check. */
public class TerrainGenerator {

    private static final int WORLD_SEED = 1235;
    private static final float SCALE = 0.01f;
    private static final float AMPLITUDE = 40f;
    private static final float MIN_HEIGHT = 70f;

    private static final float CAVE_SCALE = 0.03f,
                               CAVE_CUTOFF = 0.2f;

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
            heightMap[chunkX][chunkZ] = (int) (MIN_HEIGHT + AMPLITUDE *
                            (1 + heightMapRandom.evaluateNoise(worldPos.x * SCALE, worldPos.z * SCALE)) / 2f);
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

    private static byte getOre(Vector3i pos, FastSimplexNoiseGenerator generator) {
        float noise = (float) (1f + generator.evaluateNoise(pos.x, pos.y, pos.z)) / 2f;

        if (noise < 0.01f) return Block.DIAMOND_ORE.getID();
        else if (noise < 0.05f) return Block.GOLD_ORE.getID();
        else if (noise < 0.1f) return Block.IRON_ORE.getID();
        else if (noise < 0.2f) return Block.COAL_ORE.getID();
        else return Block.STONE.getID();
    }

    private static void generateFirstpass(Chunk chunk) {
        // generate blocks
        byte[] blocks = new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE];
        boolean isAllAir = true;

        int[][] heightMap = generateHeightmap(chunk);

        var caveGenerator = getNoiseGenerator();
        var oreGenerator = getNoiseGenerator();
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
            } else if (worldPos.y <= terrainLevel-3){
                if (isCave(worldPos, caveGenerator)) block = Block.AIR.getID();
                else                                 block = getOre(worldPos, oreGenerator);
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
