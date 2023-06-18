package chunk;

import biome.Biome;
import biomes.ForestBiome;
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
    private static Biome biome = new ForestBiome();

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
        var structures = biome.getStructures();

        Random random = new Random();
        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++)
        {
            random.setSeed(STRUCTURE_SEED ^ (x * 10923L + y * 9234L + z * 98259L));
            byte block = chunk.getBlock(x, y, z);

            for (Biome.StructureSpawnInfo info : structures) {
                if (block == info.spawnBlock().getID()
                    && random.nextFloat() < info.chance()) {
                    info.structure().make(chunk, new Vector3i(x, y, z), random::nextFloat);
                }
            }
        }

        chunk.setStatus(Chunk.Status.BLOCKS_GENERATED);
    }

}
