package chunk;

import biome.Biome;
import entity.EntityManager;
import org.joml.Vector3i;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StructureGenerator {

    // NOTE: you can only have 1 structuregenerator running at once, as it modifies multiple chunks, and having two could overlap and break things.
    // it may be threaded but can only be 1 thread!

    private static Thread thread = new Thread(StructureGenerator::run);
    private static boolean running = true;
    static { thread.start(); }

    private static BlockingQueue<Chunk> loadQueue = new LinkedBlockingQueue<>();

    private static long STRUCTURE_SEED = 1349L;

    public static void addChunk(Chunk chunk) {
        loadQueue.add(chunk);
    }

    public static int getQueueSize() {
        return loadQueue.size();
    }

    public static void stop() {
        running = false;
        try {
            thread.interrupt();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void run() {
        System.out.println("StructureGenerator running");
        while (running) {
            try {
                var chunk = loadQueue.take();
                loadChunk(chunk);
            } catch (InterruptedException e) {}
        }
        System.out.println("StructureGenerator stopped");
    }

    public static void loadChunk(Chunk chunk) {
        var terrainMapDataComponent = EntityManager.getComponent(chunk, TerrainMapDataComponent.class);
        Biome[][] biomeMap = terrainMapDataComponent.getBiomeMap();
        int[][] heightMap = terrainMapDataComponent.getHeightMap();

        Random random = new Random();
        for (int x = 0; x < Chunk.SIZE; x++)
        for (int z = 0; z < Chunk.SIZE; z++)
        {
            int height = heightMap[x][z];
            int y = height & (Chunk.SIZE-1);
            int minHeight = chunk.getChunkGridPos().y * Chunk.SIZE;
            int maxHeight = minHeight + Chunk.SIZE - 1;

            if (!(minHeight < height && height < maxHeight)) {
                continue;
            }

            random.setSeed(STRUCTURE_SEED ^ (x * 10923L + y * 9234L + z * 98259L));
            byte block = chunk.getBlock(x, y, z);

            Biome biome = biomeMap[x][z];
            var structures = biome.getStructures();

            for (Biome.StructureSpawnInfo info : structures) {
                if (block == info.spawnBlock().getID()
                    && random.nextFloat() < info.chance()) {
                    info.structure().make(chunk, new Vector3i(x, y, z), random::nextFloat);
                }
            }
        }

        chunk.setStatus(Chunk.Status.BLOCKS_GENERATED);
        EntityManager.removeComponent(chunk, TerrainMapDataComponent.class);
    }

}
