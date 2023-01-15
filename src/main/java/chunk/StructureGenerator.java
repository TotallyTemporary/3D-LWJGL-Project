package chunk;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StructureGenerator {

    // NOTE: you can only have 1 structuregenerator running at once, as it modifies multiple chunks, and having two could overlap and break things.
    // it may be threaded but can only be 1 thread!

    private static Thread thread = new Thread(StructureGenerator::run);
    private static boolean running = true;
    static { thread.start(); }

    private static BlockingQueue<Chunk> loadQueue = new LinkedBlockingQueue<>();

    public static void addChunk(Chunk chunk) {
        loadQueue.add(chunk);
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
        while (running) {
            try {
                var chunk = loadQueue.take();
                loadChunk(chunk);
            } catch (InterruptedException e) {}
        }
    }

    public static void loadChunk(Chunk chunk) {
        var random = new Random();

        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++)
        {
            if (chunk.getBlock(x, y, z) == Block.GRASS.getID()) {
                if (random.nextFloat() < 0.01f) {
                    chunk.setBlockSafe(x, y+1, z, Block.OAK_LOG.getID());
                    chunk.setBlockSafe(x, y+2, z, Block.OAK_LOG.getID());
                    chunk.setBlockSafe(x, y+3, z, Block.OAK_LOG.getID());

                    for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                    for (int dz = -1; dz <= 1; dz++)
                    {
                        chunk.setBlockSafe(x+dx, y+dy+4, z+dz, Block.OAK_LEAVES.getID());
                    }
                } else if (random.nextFloat() < 0.05f) {
                    chunk.setBlockSafe(x, y+1, z, Block.DANDELION.getID());
                }
            }
        }

        chunk.setStatus(Chunk.Status.LOADED);
    }

}
