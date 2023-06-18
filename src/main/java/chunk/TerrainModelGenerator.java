package chunk;

import entity.EntityManager;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import org.joml.Vector3i;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/** Loads a block array into model data. */
public class TerrainModelGenerator {

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
    static { System.out.println("TerrainModelGenerator running"); }

    // 6 lists, one for every cardinal direction.
    private static final ThreadLocal<FloatArrayList[]> verticesBufferLocal = ThreadLocal.withInitial(() -> new FloatArrayList[] {
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList() });
    private static final ThreadLocal<FloatArrayList[]> textureCoordsBufferLocal = ThreadLocal.withInitial(() -> new FloatArrayList[] {
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList() });
    private static final ThreadLocal<FloatArrayList[]> coloursBufferLocal = ThreadLocal.withInitial(() -> new FloatArrayList[] {
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList(),
            new FloatArrayList(), new FloatArrayList() });

    // adds a chunk to the queue to be loaded in the future
    public static void addChunk(Chunk chunk) {
        executor.submit(() -> loadChunk(chunk));
    }

    // loads a chunk immediately
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
        // clear buffers
        var verticesBuffer = verticesBufferLocal.get();
        var textureCoordsBuffer = textureCoordsBufferLocal.get();
        var lightBuffer = coloursBufferLocal.get();

        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            verticesBuffer[i].clear();
            textureCoordsBuffer[i].clear();
            lightBuffer[i].clear();
        }

        // go through every block and every face
        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++) {
            var block = Block.getBlock(chunk.getBlock(x, y, z));
            for (int index = 0; index < CardinalDirection.COUNT; index++) {

                var face = block.getFace(index);
                if (face == null) continue;
                if (!face.isTransparent() && !isFaceVisible(chunk, index, x, y, z)) continue;

                byte faceLight;

                // transparent faces get their colour from inside the block, not next to it
                if (face.isTransparent()) {
                    faceLight = chunk.getColourSafe(x, y, z);
                } else {
                    var faceOffset = CardinalDirection.offsets[index];
                    faceLight = chunk.getColourSafe(x+faceOffset.x, y+faceOffset.y, z+faceOffset.z);
                }

                // get vertices of a face, and move them to the correct place (add block x,y,z to face x,y,z)
                float[] vertices = face.getVertices();
                float[] transformedVertices = new float[vertices.length];
                for (int i = 0; i < vertices.length/3; i++) {
                    transformedVertices[3*i  ] = (vertices[3*i  ] + x);
                    transformedVertices[3*i+1] = (vertices[3*i+1] + y);
                    transformedVertices[3*i+2] = (vertices[3*i+2] + z);
                }

                float[] light = new float[vertices.length / 3 * 2];
                for (int i = 0; i < light.length / 2; i++) {
                    light[2*i + 0] = Chunk.getBlock(faceLight) / (float) Chunk.MAX_LIGHT; // block
                    light[2*i + 1] = Chunk.getSky(faceLight) / (float) Chunk.MAX_LIGHT; // sky
                }

                verticesBuffer[face.direction].addAll(FloatArrayList.wrap(transformedVertices));
                textureCoordsBuffer[face.direction].addAll(FloatArrayList.wrap(face.getTextureCoords()));
                lightBuffer[face.direction].addAll(FloatArrayList.wrap(light));
            }
        }

        return wrapIntoModel(verticesBuffer, textureCoordsBuffer, lightBuffer);
    }

    private static ChunkModelDataComponent wrapIntoModel(FloatArrayList[] verticesBuffer, FloatArrayList[] textureCoordsBuffer, FloatArrayList[] colourBuffer) {
        // calculate total number of vertices and texture coordinates of each face
        int verticesSize = 0;
        int textureCoordsSize = 0;
        int coloursSize = 0;
        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            verticesSize += verticesBuffer[i].size();
            textureCoordsSize += textureCoordsBuffer[i].size();
            coloursSize += colourBuffer[i].size();
        }

        // generate empty floatarraylists of that size
        var vertices = FloatArrayList.wrap(new float[verticesSize]);
        var textureCoords = FloatArrayList.wrap(new float[textureCoordsSize]);
        var colours = FloatArrayList.wrap(new float[coloursSize]);
        vertices.clear();
        textureCoords.clear();
        colours.clear();

        // put all the vertices and texture coordinates into the combined lists
        // addElements is a fast arraycopy because fastutil devs are geniuses
        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            vertices.addElements(vertices.size(), verticesBuffer[i].elements(), 0, verticesBuffer[i].size());
            textureCoords.addElements(textureCoords.size(), textureCoordsBuffer[i].elements(), 0, textureCoordsBuffer[i].size());
            colours.addElements(colours.size(), colourBuffer[i].elements(), 0, colourBuffer[i].size());
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
            colours.elements(),
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
