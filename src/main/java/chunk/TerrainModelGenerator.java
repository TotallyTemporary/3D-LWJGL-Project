package chunk;

import block.Block;
import block.BlockFace;
import block.CardinalDirection;
import block.WorldBlockFace;
import entity.EntityManager;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/** Loads a block array into model data. */
public class TerrainModelGenerator {

    private static final float GAMMA = 2.2f;
    private static final float MIN_LIGHT = 0.01f;

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
    static { System.out.println("TerrainModelGenerator running"); }

    // 6 lists, one for every cardinal direction.
    private static final ThreadLocal<FloatArrayList[]> verticesBufferLocal = ThreadLocal.withInitial(() -> createBufferArray(CardinalDirection.COUNT));
    private static final ThreadLocal<FloatArrayList[]> textureCoordsBufferLocal = ThreadLocal.withInitial(() -> createBufferArray(CardinalDirection.COUNT));
    private static final ThreadLocal<FloatArrayList[]> coloursBufferLocal = ThreadLocal.withInitial(() -> createBufferArray(CardinalDirection.COUNT));

    // alpha blended vertices are separated
    private static final ThreadLocal<FloatArrayList> alphaBlendVerticesBufferLocal = ThreadLocal.withInitial(() -> new FloatArrayList());
    private static final ThreadLocal<FloatArrayList> alphaBlendTextureCoordsBufferLocal = ThreadLocal.withInitial(() -> new FloatArrayList());
    private static final ThreadLocal<FloatArrayList> alphaBlendColoursBufferLocal = ThreadLocal.withInitial(() -> new FloatArrayList());

    private static final FloatArrayList[] createBufferArray(int count) {
        FloatArrayList[] array = new FloatArrayList[count];
        for (int i = 0; i < count; i++) {
            array[i] = new FloatArrayList();
        }
        return array;
    }

    private static final float[] fakeBlockSideLights = new float[] {
        1f,    // up
        0.98f, // left
        0.95f, // front
        0.92f, // back
        0.92f, // right
        0.88f  // down
    };

    // adds a chunk to the queue to be loaded in the future
    public static void addChunk(Chunk chunk) {
        executor.submit(() -> loadChunk(chunk));
    }

    // loads a chunk immediately
    public static void loadChunk(Chunk chunk) {
        try {
            var comp = generateModelData(chunk);
            EntityManager.addComponent(chunk, comp);
            chunk.setStatus(Chunk.Status.MESH_GENERATED);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        var alphaBlendVerticesBuffer = alphaBlendVerticesBufferLocal.get();
        var alphaBlendTextureCoordsBuffer = alphaBlendTextureCoordsBufferLocal.get();
        var alphaBlendLightBuffer = alphaBlendColoursBufferLocal.get();

        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            verticesBuffer[i].clear();
            textureCoordsBuffer[i].clear();
            lightBuffer[i].clear();
        }
        alphaBlendVerticesBuffer.clear();
        alphaBlendTextureCoordsBuffer.clear();
        alphaBlendLightBuffer.clear();

        for (int dir : new int[] {
            CardinalDirection.RIGHT, // x
            CardinalDirection.UP,    // y
            CardinalDirection.BACK   // z
        }) {
            Vector3i normal = CardinalDirection.offsets[dir];
            Vector3i tangent = CardinalDirection.tangents[dir];
            Vector3i biTangent = CardinalDirection.biTangents[dir];
            WorldBlockFace[][] sliceMask = new WorldBlockFace[Chunk.SIZE][Chunk.SIZE];

            // runs twice, isBackface=true then isBackface=false.
            // horrible construct but it works :)
            for (boolean isBackface = true, b = false; b != isBackface; isBackface = isBackface && b, b = !b) {
                // for down direction use the same normal,tangent,bitangent as up, but the blockface being checked is different.
                dir = CardinalDirection.opposite(dir);

                for (int slice = 0; slice < Chunk.SIZE; slice++) {
                    // cursor starts at 0,0 on our slice
                    Vector3i cursor = new Vector3i();
                    cursor.add(normal);
                    cursor.mul(slice);

                    // go through our slice and get all of the block faces on it
                    for (int tan = 0; tan < Chunk.SIZE; tan++) {
                        for (int biTan = 0; biTan < Chunk.SIZE; biTan++) {
                            // do occlusion culling
                            byte blockID = chunk.getBlock(cursor.x, cursor.y, cursor.z);
                            Block block = Block.getBlock(blockID);
                            BlockFace face = block.getFace(dir);
                            if (shouldAddFace(chunk, cursor.x, cursor.y, cursor.z, blockID, dir, face)) {
                                byte light = getLight(chunk, cursor.x, cursor.y, cursor.z, face);
                                var worldFace = new WorldBlockFace(face, light);
                                sliceMask[tan][biTan] = worldFace;
                            } else {
                                sliceMask[tan][biTan] = null;
                            }

                            cursor.add(biTangent); // increment
                        }

                        // bitangent is at its end, set it back to 0 before we loop again
                        Vector3i antiBitangent = new Vector3i(1 - biTangent.x, 1 - biTangent.y, 1 - biTangent.z);
                        cursor.mul(antiBitangent);

                        // increment tangent
                        cursor.add(tangent);
                    }

                    // now we have a mask, or a 2d array of all the block faces on this slice we want to render

                    // go through the 2d mask and try to greedily create big rectangles on it
                    // note that the x and y aren't actual coordinates in world space
                    // x and y are basically our new "cursor"
                    for (int y = 0; y < Chunk.SIZE; y++) {
                        for (int x = 0; x < Chunk.SIZE; ) {
                            // iterate into the first block face
                            if (sliceMask[x][y] == null) {
                                x++;
                                continue;
                            }

                            // we are on a block face. let's try to extend it to the "right".
                            WorldBlockFace face = sliceMask[x][y];
                            int width = 1;
                            while (x + width < Chunk.SIZE) {
                                var blockFace = sliceMask[x + width][y];
                                if (blockFace == null || !blockFace.equals(face)) {
                                    break;
                                }
                                width++;
                            }

                            // now let's try extend it down.
                            int height = 1;
                            while (y + height < Chunk.SIZE) {
                                // check for all of the faces on the row. if one doesn't match, we're out
                                boolean foundBadFace = false;
                                for (int k = x; k < x + width; k++) {
                                    var blockFace = sliceMask[k][y + height];
                                    if (blockFace == null || !blockFace.equals(face)) {
                                        foundBadFace = true;
                                    }
                                }

                                if (foundBadFace) {
                                    break;
                                }

                                height++;
                            }

                            // we have our x,y,width,height for the quad we're making

                            // clear the mask in this area
                            for (int v = y; v < y + height; v++)
                            for (int u = x; u < x + width; u++)
                            {
                                sliceMask[u][v] = null;
                            }

                            // convert x,y,slice into world coordinates
                            Vector3i world = new Vector3i();
                            world.add(tangent.mul(x, new Vector3i())); // move x units on the tangent
                            world.add(biTangent.mul(y, new Vector3i())); // move y units on the bitangent
                            world.add(normal.mul(slice, new Vector3i())); // move slice units on the normal

                            // add the face!
                            addFace(chunk,
                                    world.x, world.y, world.z,
                                    tangent, width,
                                    biTangent, height,
                                    face,
                                    verticesBuffer, textureCoordsBuffer, lightBuffer,
                                    alphaBlendVerticesBuffer, alphaBlendTextureCoordsBuffer, alphaBlendLightBuffer);

                            x += width;
                        }
                    }
                }
            }
        }

        return wrapIntoModel(verticesBuffer, textureCoordsBuffer, lightBuffer,
                            alphaBlendVerticesBuffer, alphaBlendTextureCoordsBuffer, alphaBlendLightBuffer);
    }

    private static ChunkModelDataComponent wrapIntoModel(
            FloatArrayList[] verticesBuffer,
            FloatArrayList[] textureCoordsBuffer,
            FloatArrayList[] colourBuffer,
            FloatArrayList alphaBlendVerticesBuffer,
            FloatArrayList alphaBlendTextureCoordsBuffer,
            FloatArrayList alphaBlendColourBuffer
    ) {
        // calculate total number of vertices and texture coordinates of each face
        int verticesSize = 0;
        int textureCoordsSize = 0;
        int coloursSize = 0;
        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            verticesSize += verticesBuffer[i].size();
            textureCoordsSize += textureCoordsBuffer[i].size();
            coloursSize += colourBuffer[i].size();
        }
        verticesSize += alphaBlendVerticesBuffer.size();
        textureCoordsSize += alphaBlendTextureCoordsBuffer.size();
        coloursSize += alphaBlendColourBuffer.size();

        // generate empty floatarraylists of that size
        var vertices = FloatArrayList.wrap(new float[verticesSize]);
        var textureCoords = FloatArrayList.wrap(new float[textureCoordsSize]);
        var colours = FloatArrayList.wrap(new float[coloursSize]);
        vertices.clear();
        textureCoords.clear();
        colours.clear();

        // put all the vertices and texture coordinates into the combined lists
        // addElements is a fast arraycopy because fastutil devs are geniuses

        // alpha blended faces are put first
        vertices.addElements(vertices.size(), alphaBlendVerticesBuffer.elements(), 0, alphaBlendVerticesBuffer.size());
        textureCoords.addElements(textureCoords.size(), alphaBlendTextureCoordsBuffer.elements(), 0, alphaBlendTextureCoordsBuffer.size());
        colours.addElements(colours.size(), alphaBlendColourBuffer.elements(), 0, alphaBlendColourBuffer.size());

        // next add normal opaque faces, face by face
        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            vertices.addElements(vertices.size(), verticesBuffer[i].elements(), 0, verticesBuffer[i].size());
            textureCoords.addElements(textureCoords.size(), textureCoordsBuffer[i].elements(), 0, textureCoordsBuffer[i].size());
            colours.addElements(colours.size(), colourBuffer[i].elements(), 0, colourBuffer[i].size());
        }

        // calculate indices (working with arrays is difficult ok?)
        // index refers to the start position of any face list.
        int[] indices = new int[CardinalDirection.COUNT];
        indices[0] = alphaBlendVerticesBuffer.size() / 3; // first segment is reserved for alpha blended faces
        indices[1] = verticesBuffer[0].size() / 3 + indices[0];
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

    private static void addFace(Chunk chunk, int x, int y, int z,
                                Vector3i widthAxis, int w,
                                Vector3i heightAxis, int h,
                                WorldBlockFace worldFace,
            FloatArrayList[] verticesBuffer,
            FloatArrayList[] textureCoordsBuffer,
            FloatArrayList[] colourBuffer,
            FloatArrayList alphaBlendVerticesBuffer,
            FloatArrayList alphaBlendTextureCoordsBuffer,
            FloatArrayList alphaBlendColourBuffer) {

        BlockFace face = worldFace.getBlockFaceDefinition();
        byte faceLight = worldFace.getLight();

        // get vertices of a face, and move them to the correct place (add block x,y,z to face x,y,z)
        float[] vertices = face.getVertices();
        float[] transformedVertices = new float[vertices.length];
        for (int i = 0; i < vertices.length/3; i++) {
            Vector3f vertex = new Vector3f(
                vertices[3*i  ],
                vertices[3*i+1],
                vertices[3*i+2]
            );
            stretch(vertex, widthAxis, w);
            stretch(vertex, heightAxis, h);
            vertex.add(x, y, z);

            transformedVertices[3*i  ] = vertex.x;
            transformedVertices[3*i+1] = vertex.y;
            transformedVertices[3*i+2] = vertex.z;
        }

        // w = 3, (0, 1, 0)
        // h = 2, (0, 0, 1)

        float[] textureCoords = face.getTextureCoords();
        float[] transformedTextureCoords = new float[textureCoords.length];
        for (int i = 0; i < textureCoords.length/3; i++) {
            transformedTextureCoords[3*i  ] = textureCoords[3*i  ] * w;
            transformedTextureCoords[3*i+1] = textureCoords[3*i+1] * h;
            transformedTextureCoords[3*i+2] = textureCoords[3*i+2];
        }

        float fakeLightMultiplier = fakeBlockSideLights[face.direction];

        float[] light = new float[vertices.length / 3 * 2];
        for (int i = 0; i < light.length / 2; i++) {
            float blockLight = Chunk.getBlock(faceLight) / (float) Chunk.MAX_LIGHT;
            blockLight *= fakeLightMultiplier;
            blockLight = (float) Math.pow(blockLight, GAMMA);
            light[2*i + 0] = blockLight; // block

            float skyLight = Chunk.getSky(faceLight) / (float) Chunk.MAX_LIGHT;
            skyLight *= fakeLightMultiplier;
            skyLight = (float) Math.pow(skyLight, GAMMA);
            skyLight = Math.max(skyLight, MIN_LIGHT); // moonlight
            light[2*i + 1] = skyLight; // sky
        }

        if (!face.isAlphaBlended()) {
            // add normal vertices to the face direction bufferQ
            verticesBuffer[face.direction].addAll(FloatArrayList.wrap(transformedVertices));
            textureCoordsBuffer[face.direction].addAll(FloatArrayList.wrap(transformedTextureCoords));
            colourBuffer[face.direction].addAll(FloatArrayList.wrap(light));
        } else {
            // face is alpha blended, we need to put this in the other buffer.
            alphaBlendVerticesBuffer.addAll(FloatArrayList.wrap(transformedVertices));
            alphaBlendTextureCoordsBuffer.addAll(FloatArrayList.wrap(transformedTextureCoords));
            alphaBlendColourBuffer.addAll(FloatArrayList.wrap(light));
        }
    }

    private static boolean shouldAddFace(Chunk chunk, int x, int y, int z, byte blockID, int faceIndex, BlockFace face) {
        // null faces, or faces that don't exist, such as air blocks, shouldn't be added
        if (face == null) {
            return false;
        }
        boolean isTransparent = face.isTransparent();

        // opaque blocks facing other opaque blocks shouldn't get their face added
        if (!isTransparent && !isFaceVisible(chunk, faceIndex, x, y, z)) {
            return false;
        }

        // cull faces that face another block of the same type
        if (isTransparent && isFacingSameBlockType(chunk, faceIndex, x, y, z, blockID)) {
            return false;
        }

        return true;
    }

    private static boolean isFacingSameBlockType(Chunk chunk, int faceIndex, int x, int y, int z, byte block) {
        var obsBlockPos = new Vector3i(x, y, z).add(CardinalDirection.offsets[faceIndex]);
        var obscuringBlock = chunk.getBlockSafe(obsBlockPos);

        return obscuringBlock == block;
    }

    private static boolean isFaceVisible(Chunk chunk, int faceIndex, int x, int y, int z) {
        var obsBlockPos = new Vector3i(x, y, z).add(CardinalDirection.offsets[faceIndex]);
        var obscuringBlock = Block.getBlock(chunk.getBlockSafe(obsBlockPos));

        if (!obscuringBlock.getHasTransparentFace()) return false;

        var oppositeFace = obscuringBlock.getFace(CardinalDirection.opposite(faceIndex));
        if (oppositeFace != null && !oppositeFace.isTransparent()) return false;

        return true;
    }

    private static byte getLight(Chunk chunk, int x, int y, int z, BlockFace face) {
        byte faceLight;
        // transparent faces get their colour from inside the block, not next to it
        if (face.isTransparent()) {
            faceLight = chunk.getColourSafe(x, y, z);
        } else {
            var faceOffset = CardinalDirection.offsets[face.direction];
            faceLight = chunk.getColourSafe(x+faceOffset.x, y+faceOffset.y, z+faceOffset.z);
        }

        return faceLight;
    }

    private static Vector3f stretch(Vector3f source, Vector3i mask, int stretch) {
        // multiply source by stretch, but only if mask for that axis is non-zero.

        source.x = source.x * (mask.x > 0 ? stretch : 1);
        source.y = source.y * (mask.y > 0 ? stretch : 1);
        source.z = source.z * (mask.z > 0 ? stretch : 1);
        return source;
    }
}
