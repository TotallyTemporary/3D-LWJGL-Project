package chunk;

import entity.EntityManager;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import org.joml.Vector3i;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TerrainModelGenerator {

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
    static { System.out.println("TerrainModelGenerator running"); }

    private static final ThreadLocal<FloatArrayList[]> verticesBufferLocal = ThreadLocal.withInitial(() -> new FloatArrayList[] {
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList() });
    private static final ThreadLocal<FloatArrayList[]> textureCoordsBufferLocal = ThreadLocal.withInitial(() -> new FloatArrayList[] {
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList() });

    // adds a chunk to the queue
    public static void addChunk(Chunk chunk) {
        executor.submit(() -> loadChunk(chunk));
    }

    public static void loadChunk(Chunk chunk) {
        var comp = generateModelData(chunk);
        EntityManager.addComponent(chunk, comp);
        chunk.setStatus(Chunk.Status.MESH_GENERATED);
    }

    public static int getQueueSize() {
        return executor.getQueue().size();
    }

    public static void stop() {
        executor.shutdownNow();
        System.out.println("TerrainModelGenerator stopped");
    }

    private static ChunkModelDataComponent generateModelData(Chunk chunk) {
        var verticesBuffer = verticesBufferLocal.get();
        var textureCoordsBuffer = textureCoordsBufferLocal.get();
        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            verticesBuffer[i].clear();
            textureCoordsBuffer[i].clear();
        }

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

                verticesBuffer[face.direction].addAll(FloatArrayList.wrap(transformedVertices));
                textureCoordsBuffer[face.direction].addAll(FloatArrayList.wrap(face.getTextureCoords()));
            }
        }

        return wrapIntoModel(verticesBuffer, textureCoordsBuffer);
    }

    private static ChunkModelDataComponent wrapIntoModel(FloatArrayList[] verticesBuffer, FloatArrayList[] textureCoordsBuffer) {
        // calculate total number of vertices and texture coordinates of each face
        int verticesSize = 0;
        int textureCoordsSize = 0;
        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            verticesSize += verticesBuffer[i].size();
            textureCoordsSize += textureCoordsBuffer[i].size();
        }

        // generate empty floatarraylists of that size
        var vertices = FloatArrayList.wrap(new float[verticesSize]);
        var textureCoords = FloatArrayList.wrap(new float[textureCoordsSize]);
        vertices.clear();
        textureCoords.clear();

        // put all the vertices and texture coordinates into the combined lists
        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            vertices.addElements(vertices.size(), verticesBuffer[i].elements(), 0, verticesBuffer[i].size());
            textureCoords.addElements(textureCoords.size(), textureCoordsBuffer[i].elements(), 0, textureCoordsBuffer[i].size());
        }

        // calculate indices (working with arrays is difficult ok?)
        // index refers to the start position of any face list.
        int[] indices = new int[CardinalDirection.COUNT];
        indices[0] = 0;
        indices[1] = verticesBuffer[0].size() / 3;
        indices[2] = verticesBuffer[1].size() / 3 + indices[1];
        indices[3] = verticesBuffer[2].size() / 3 + indices[2];
        indices[4] = verticesBuffer[3].size() / 3 + indices[3];
        indices[5] = verticesBuffer[4].size() / 3 + indices[4];

        // put those two full lists into the component
        return new ChunkModelDataComponent(
            vertices.elements(),
            textureCoords.elements(),
            indices
        );
    }

    private static boolean isFaceVisible(Chunk chunk, int faceIndex, int x, int y, int z) {
        var obsBlockPos = new Vector3i(x, y, z).add(CardinalDirection.offsets[faceIndex]);
        var obscuringBlock = Block.getBlock(chunk.getBlockSafe(obsBlockPos));

        if (!obscuringBlock.getHasTransparentFace()) return false;

        var oppositeFace = obscuringBlock.getFace(CardinalDirection.opposite(faceIndex));
        if (oppositeFace != null && !oppositeFace.isTransparent()) return false;

        return true;
    }
}
