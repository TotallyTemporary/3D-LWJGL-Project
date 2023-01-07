package chunk;

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
                chunk.setStatus(Chunk.Status.LOADED);
            } catch (InterruptedException e) {}
        }
    }

    private static void loadChunk(Chunk chunk) {

    }

}
