package chunk;

import entity.EntityManager;
import org.boon.collections.FloatList;
import org.joml.Vector3i;

import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TerrainModelGenerator {

    public record Tuple<X, Y>(X x, Y y) {}

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    private static final ThreadLocal<FloatList> verticesBufferLocal = ThreadLocal.withInitial(() -> new FloatList());
    private static final ThreadLocal<FloatList> textureCoordsBufferLocal = ThreadLocal.withInitial(() -> new FloatList());

    // adds a chunk to the queue
    public static void addChunk(Chunk chunk) {
        executor.submit(() -> {
            var comp = generateModelData(chunk);
            EntityManager.addComponent(chunk, comp);
            chunk.setStatus(Chunk.Status.PREPARED);
        });
    }

    private static ChunkModelDataComponent generateModelData(Chunk chunk) {
        var verticesBuffer = verticesBufferLocal.get();
        var textureCoordsBuffer = textureCoordsBufferLocal.get();

        resetFloatList(verticesBuffer);
        resetFloatList(textureCoordsBuffer);

        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++) {
            var block = Block.getBlock(chunk.getBlock(x, y, z));
            for (int index = 0; index < 6; index++) {
                var face = block.getFace(index);
                if (face == null) continue;
                if (!isFaceVisible(chunk, index, x, y, z)) continue;

                float[] vertices = face.getVertices();
                float[] transformedVertices = new float[vertices.length];
                for (int i = 0; i < vertices.length/3; i++) {
                    transformedVertices[3*i  ] = (vertices[3*i  ] + x);
                    transformedVertices[3*i+1] = (vertices[3*i+1] + y);
                    transformedVertices[3*i+2] = (vertices[3*i+2] + z);
                }
                verticesBuffer.addArray(transformedVertices);
                textureCoordsBuffer.addArray(face.getTextureCoords());
            }
        }

        return new ChunkModelDataComponent(
            verticesBuffer.toValueArray(),
            textureCoordsBuffer.toValueArray()
        );
    }

    private static boolean isFaceVisible(Chunk chunk, int faceIndex, int x, int y, int z) {
        var obscuringBlock = Block.getBlock(switch (faceIndex) {
            case Direction.UP    -> getBlockSafe(chunk, new Vector3i(x, y+1, z));
            case Direction.DOWN  -> getBlockSafe(chunk, new Vector3i(x, y-1, z));
            case Direction.LEFT  -> getBlockSafe(chunk, new Vector3i(x-1, y, z));
            case Direction.RIGHT -> getBlockSafe(chunk, new Vector3i(x+1, y, z));
            case Direction.FRONT -> getBlockSafe(chunk, new Vector3i(x, y, z-1));
            case Direction.BACK  -> getBlockSafe(chunk, new Vector3i(x, y, z+1));
            default -> throw new IllegalStateException("Unexpected value: " + faceIndex);
        });

        if (!obscuringBlock.getHasTransparentFace()) return false;

        var oppositeFace = obscuringBlock.getFace(Direction.opposite(faceIndex));
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
    /* maybe use this if toValueArray is slow? visualvm shows it as slow, intellij doesn't.
    private static float[] getValues(FloatList lst) throws NoSuchFieldException, IllegalAccessException {
        int end = lst.size();
        Field values = lst.getClass().getDeclaredField("values");
        values.setAccessible(true);
        float[] arr = (float[]) values.get(lst);
        float[] newArr = new float[lst.size()];
        System.arraycopy(arr, 0, newArr, 0, lst.size());
        return newArr;
    }
     */
}
