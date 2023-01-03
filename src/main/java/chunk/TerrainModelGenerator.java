package chunk;

import entity.EntityManager;
import org.boon.collections.FloatList;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TerrainModelGenerator {

    public record Tuple<X, Y>(X x, Y y) {}

    private static Thread thread;
    private static boolean running = false;

    private static final LinkedBlockingQueue<Chunk> chunkLoadQueue = new LinkedBlockingQueue<>();
    private static final ConcurrentLinkedQueue<Tuple<Chunk, ChunkModelDataComponent>> doneQueue = new ConcurrentLinkedQueue<>();

    private static FloatList verticesBuffer = new FloatList();
    private static FloatList textureCoordsBuffer = new FloatList();

    public static void addChunk(Chunk chunk) {
        if (!chunkLoadQueue.contains(chunk)) {
            chunkLoadQueue.add(chunk);
        }
    }

    public static void removeChunks() {
        Tuple<Chunk, ChunkModelDataComponent> entry;
        while ((entry = doneQueue.poll()) != null) {
            EntityManager.addComponent(entry.x, entry.y);
            entry.x.setStatus(Chunk.Status.PREPARED);
        }
    }

    public static void start() {
        running = true;
        thread = new Thread(TerrainModelGenerator::loadChunks);
        thread.start();
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

    private static void loadChunks() {
        while (running) {
            try {
                var chunk = chunkLoadQueue.take();
                var data = generateModelData(chunk);
                doneQueue.add(new Tuple(chunk, data));
            } catch (InterruptedException e) {}
        }
    }

    private static ChunkModelDataComponent generateModelData(Chunk chunk) {
        resetFloatList(verticesBuffer);
        resetFloatList(textureCoordsBuffer);

        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++) {
            var block = Block.getBlock(chunk.getBlock(x, y, z));
            for (int index = 0; index < 6; index++) {
                var face = block.getFace(index);
                if (!isFaceVisible(chunk, face, x, y, z)) continue;

                for (var vertex : to3DVectors(face.getVertices())) {
                    verticesBuffer.add(vertex.x + x);
                    verticesBuffer.add(vertex.y + y);
                    verticesBuffer.add(vertex.z + z);
                }
                textureCoordsBuffer.addArray(face.getTextureCoords());
            }
        }

        return new ChunkModelDataComponent(
            verticesBuffer.toValueArray(),
            textureCoordsBuffer.toValueArray()
        );
    }

    private static ArrayList<Vector3f> to3DVectors(float[] somePositions) {
        var lst = new ArrayList<Vector3f>();
        for (int i = 0; i < somePositions.length/3; i++) {
            lst.add(new Vector3f(
                    somePositions[i*3  ],
                    somePositions[i*3+1],
                    somePositions[i*3+2]
            ));
        }

        return lst;
    }

    private static boolean isFaceVisible(Chunk chunk, BlockFace face, int x, int y, int z) {
        if (face == null) return false;

        var obscuringBlock = Block.getBlock(switch (face.direction) {
            case UP    -> getBlockSafe(chunk, new Vector3i(x, y+1, z));
            case DOWN  -> getBlockSafe(chunk, new Vector3i(x, y-1, z));
            case LEFT  -> getBlockSafe(chunk, new Vector3i(x-1, y, z));
            case RIGHT -> getBlockSafe(chunk, new Vector3i(x+1, y, z));
            case FRONT -> getBlockSafe(chunk, new Vector3i(x, y, z-1));
            case BACK  -> getBlockSafe(chunk, new Vector3i(x, y, z+1));
        });

        if (!obscuringBlock.getHasTransparentFace()) return false;

        var oppositeDirection = face.direction.opposite().ordinal();
        var oppositeFace = obscuringBlock.getFace(oppositeDirection);
        if (oppositeFace == null || oppositeFace.isTransparent()) {
            return true;
        }

        return true;
    }

    private static byte getBlockSafe(Chunk chunk, Vector3i pos) {
        var block = chunk.getBlock(pos);
        if (block == Block.INVALID.getID()) {
            var worldPos = Chunk.blockPosToWorldPos(pos, chunk);
            block = ChunkLoader.getBlockAt(worldPos);
        }
        return block;
    }


    private static void resetFloatList(FloatList lst) {
        try {
            Field end = lst.getClass().getDeclaredField("end");
            end.setAccessible(true);
            end.setInt(lst, 0);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
