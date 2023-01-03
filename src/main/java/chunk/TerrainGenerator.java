package chunk;

import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TerrainGenerator {

    private static final LinkedBlockingQueue<Chunk> chunkGenQueue = new LinkedBlockingQueue<>();
    private static final ConcurrentLinkedQueue<Chunk> doneQueue = new ConcurrentLinkedQueue<>();

    private static Thread thread;
    private static boolean running = false;

    public static void start() {
        running = true;
        thread = new Thread(TerrainGenerator::loadChunks);
        thread.start();
    }

    public static void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void addChunk(Chunk chunk) {
        if (!chunkGenQueue.contains(chunk)) {
            chunkGenQueue.add(chunk);
        }
    }

    public static void removeChunks() {
        Chunk chunk;
        while ((chunk = doneQueue.poll()) != null) {
            chunk.setStatus(Chunk.Status.WAIT_NEIGHBORS);
        }
    }

    private static void loadChunks() {
        while (running) {
            try {
                var chunk = chunkGenQueue.take();
                generateTerrain(chunk);
                doneQueue.add(chunk);
            } catch (InterruptedException e) {}
        }
    }

    private static void generateTerrain(Chunk chunk) {
        // generate blocks
        byte[] blocks = new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE];
        boolean isAllAir = true;

        for (int chunkX = 0; chunkX < Chunk.SIZE; chunkX++)
        for (int chunkY = 0; chunkY < Chunk.SIZE; chunkY++)
        for (int chunkZ = 0; chunkZ < Chunk.SIZE; chunkZ++)
        {
            var index = Chunk.toIndex(chunkX, chunkY, chunkZ);
            var worldPos = Chunk.blockPosToWorldPos(new Vector3i(chunkX, chunkY, chunkZ), chunk);
            var terrainLevel = 70 + (int) (10 * Math.sin((worldPos.x + worldPos.z)/100d));

            var block = Block.AIR.getID();


            if (worldPos.y == terrainLevel) {
                block = Block.GRASS.getID();
            } else if (terrainLevel-3 < worldPos.y && worldPos.y < terrainLevel) {
                block = Block.DIRT.getID();
            } else if (0 < worldPos.y && worldPos.y <= terrainLevel-3){
                block = Block.STONE.getID();
            } else if (worldPos.y == 0) {
                block = Block.BEDROCK.getID();
            }

            if (block != Block.AIR.getID()) isAllAir = false;
            blocks[index] = block;
        }
        if (isAllAir) chunk.isAllAir();
        else chunk.setBlocks(blocks);
    }
}
