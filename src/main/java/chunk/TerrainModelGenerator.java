package chunk;

import entity.EntityManager;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import org.joml.Vector3i;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TerrainModelGenerator {

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    private static final ThreadLocal<FloatArrayList> verticesBufferLocal = ThreadLocal.withInitial(FloatArrayList::new);
    private static final ThreadLocal<FloatArrayList> textureCoordsBufferLocal = ThreadLocal.withInitial(FloatArrayList::new);

    // adds a chunk to the queue
    public static void addChunk(Chunk chunk) {
        executor.submit(() -> loadChunk(chunk));
    }

    public static void loadChunk(Chunk chunk) {
        var comp = generateModelData(chunk);
        EntityManager.addComponent(chunk, comp);
        chunk.setStatus(Chunk.Status.PREPARED);
    }

    public static void stop() {
        executor.shutdownNow();
    }

    private static ChunkModelDataComponent generateModelData(Chunk chunk) {
        var verticesBuffer = verticesBufferLocal.get();
        var textureCoordsBuffer = textureCoordsBufferLocal.get();

        verticesBuffer.clear();
        textureCoordsBuffer.clear();

        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++) {
            var block = Block.getBlock(chunk.getBlock(x, y, z));
            for (int index = 0; index < CardinalDirection.COUNT; index++) {
                var face = block.getFace(index);
                if (face == null) continue;
                if (!face.isTransparent() && !isFaceVisible(chunk, index, x, y, z)) continue;

                float[] vertices = face.getVertices();
                float[] transformedVertices = new float[vertices.length];
                for (int i = 0; i < vertices.length/3; i++) {
                    transformedVertices[3*i  ] = (vertices[3*i  ] + x);
                    transformedVertices[3*i+1] = (vertices[3*i+1] + y);
                    transformedVertices[3*i+2] = (vertices[3*i+2] + z);
                }

                verticesBuffer.addAll(FloatArrayList.wrap(transformedVertices));
                textureCoordsBuffer.addAll(FloatArrayList.wrap(face.getTextureCoords()));
            }
        }

        return new ChunkModelDataComponent(
            verticesBuffer.toArray(new float[0]),
            textureCoordsBuffer.toArray(new float[0])
        );
    }

    private static boolean isFaceVisible(Chunk chunk, int faceIndex, int x, int y, int z) {
        var obsBlockPos = new Vector3i(x, y, z).add(CardinalDirection.offsets[faceIndex]);
        var obscuringBlock = Block.getBlock(chunk.getBlockSafe(obsBlockPos));

        if (!obscuringBlock.getHasTransparentFace()) return false;

        var oppositeFace = obscuringBlock.getFace(CardinalDirection.opposite(faceIndex));
        if (oppositeFace == null || oppositeFace.isTransparent()) {
            return true;
        }

        return true;
    }
}
