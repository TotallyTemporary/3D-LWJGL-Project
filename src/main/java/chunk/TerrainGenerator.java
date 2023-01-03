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

        for (int chunkX = 0; chunkX < Chunk.SIZE; chunkX++)
        for (int chunkY = 0; chunkY < Chunk.SIZE; chunkY++)
        for (int chunkZ = 0; chunkZ < Chunk.SIZE; chunkZ++)
        {
            var index = Chunk.toIndex(chunkX, chunkY, chunkZ);
            var worldPos = Chunk.blockPosToWorldPos(new Vector3i(chunkX, chunkY, chunkZ), chunk);
            var terrainLevel = (int) (10 * Math.sin((worldPos.x + worldPos.z)/100d));

            var block = Block.AIR.getID();

            if (worldPos.y == terrainLevel) {
                block = Block.GRASS.getID();
            } else if (terrainLevel-3 < worldPos.y && worldPos.y < terrainLevel) {
                block = Block.DIRT.getID();
            } else if (worldPos.y <= terrainLevel-3){
                block = Block.STONE.getID();
            }

            blocks[index] = block;
        }

        chunk.setBlocks(blocks);
    }
}
