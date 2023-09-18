package item;

import block.Block;
import block.BlockFace;
import block.CardinalDirection;
import block.SquareBlockFace;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import render.ArrayTexture;
import render.Model;
import shader.Shader;

import java.nio.ByteBuffer;

public class ItemModel {

    private static Shader itemShader;
    private static ArrayTexture blockTexture, itemTexture;

    public static void init(Shader _itemShader, ArrayTexture _blockTexture, ArrayTexture _itemTexture) {
        itemShader = _itemShader;
        blockTexture = _blockTexture;
        itemTexture = _itemTexture;
    }

    public static Model noModel() {
        return new Model()
            .addPosition3D(new float[] {})
            .addTextureCoords3D(new float[] {})
            .setShader(itemShader)
            .setTexture(blockTexture)
            .end();
    }

    public static Model from3DBlock(Block block) {
        var vertices = new FloatArrayList();
        var texCoords = new FloatArrayList();

        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            var face = block.getFace(i);
            if (face == null) continue;

            // move origin to be at the center of the bottom of the block
            var faceVertices = face.getVertices();
            for (int j = 0; j < faceVertices.length/3; j++) {
                vertices.add(faceVertices[3*j  ]);
                vertices.add(faceVertices[3*j+1]);
                vertices.add(faceVertices[3*j+2]);
            }

            texCoords.addElements(texCoords.size(), face.getTextureCoords());
        }

        return new Model()
            .addPosition3D(vertices.elements())
            .addTextureCoords3D(texCoords.elements())
            .setShader(itemShader)
            .setTexture(blockTexture)
            .end();
    }

    public static Model from2DBlock(Block block) {
        return from2D(block.getID(), blockTexture);
    }

    public static Model from2DItem(int itemID) {
        return from2D(itemID, itemTexture);
    }

    // takes the texture of a element in the block texture
    private static Model from2D(int blockID, ArrayTexture texture) {
        var spriteData = getSpriteDataFromTexture(blockID, texture);
        var pixels = bufferTo2DArray(spriteData, texture.getTileWidth(), texture.getTileHeight());

        var vertices = new FloatArrayList();
        var texCoords = new FloatArrayList();

        for (int y = 0; y < texture.getTileHeight(); y++) {
            for (int x = 0; x < texture.getTileWidth(); x++) {
                if (!isOpaque(getPixel(pixels, x, y))) continue;

                var front = makeSmallBlockFaceAt(x, y, CardinalDirection.FRONT, blockID, texture);
                vertices.addElements(vertices.size(), front.getVertices());
                texCoords.addElements(texCoords.size(), front.getTextureCoords());

                var back = makeSmallBlockFaceAt(x, y, CardinalDirection.BACK, blockID, texture);
                vertices.addElements(vertices.size(), back.getVertices());
                texCoords.addElements(texCoords.size(), back.getTextureCoords());

                if (!isOpaque(getPixel(pixels, x-1, y))) {
                    var face = makeSmallBlockFaceAt(x, y, CardinalDirection.LEFT, blockID, texture);
                    vertices.addElements(vertices.size(), face.getVertices());
                    texCoords.addElements(texCoords.size(), face.getTextureCoords());
                }

                if (!isOpaque(getPixel(pixels, x+1, y))) {
                    var face = makeSmallBlockFaceAt(x, y, CardinalDirection.RIGHT, blockID, texture);
                    vertices.addElements(vertices.size(), face.getVertices());
                    texCoords.addElements(texCoords.size(), face.getTextureCoords());
                }

                if (!isOpaque(getPixel(pixels, x, y+1))) {
                    var face = makeSmallBlockFaceAt(x, y, CardinalDirection.UP, blockID, texture);
                    vertices.addElements(vertices.size(), face.getVertices());
                    texCoords.addElements(texCoords.size(), face.getTextureCoords());
                }

                if (!isOpaque(getPixel(pixels, x, y-1))) {
                    var face = makeSmallBlockFaceAt(x, y, CardinalDirection.DOWN, blockID, texture);
                    vertices.addElements(vertices.size(), face.getVertices());
                    texCoords.addElements(texCoords.size(), face.getTextureCoords());
                }
            }
        }

        return new Model()
            .addPosition3D(vertices.elements())
            .addTextureCoords3D(texCoords.elements())
            .setShader(itemShader)
            .setTexture(texture)
            .end();
    }

    private static ByteBuffer getSpriteDataFromTexture(int index, ArrayTexture texture) {
        var BYTES_PER_PIXEL = 4;
        var TILE_SIZE_BYTES = texture.getTileWidth() * texture.getTileHeight() * BYTES_PER_PIXEL;

        var data = texture.getImageData();

        var start = index * TILE_SIZE_BYTES;
        byte[] sprite = new byte[TILE_SIZE_BYTES];
        data.get(start, sprite, 0, sprite.length);
        return ByteBuffer.wrap(sprite);
    }

    // also flips y
    private static int[][] bufferTo2DArray(ByteBuffer buffer, int width, int height) {
        int[][] pixels = new int[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[x][height-y-1] = buffer.getInt();
            }
        }

        return pixels;
    }

    private static int getPixel(int[][] pixels, int x, int y) {
        int xSize = pixels.length;
        int ySize = pixels[0].length;
        if (x < 0 || x >= xSize) return 0;
        if (y < 0 || y >= ySize) return 0;
        return pixels[x][y];
    }

    private static boolean isOpaque(int pixel) {
        return (pixel & 255) != 0;
    }

    private static BlockFace makeSmallBlockFaceAt(int xPixel, int yPixel, int direction, int index, ArrayTexture texture) {
        var pixelSize = 1f / texture.getTileWidth();

        var templateBlockFace = new SquareBlockFace(index, direction, BlockFace.NO_FLAG);

        var verts = templateBlockFace.getVertices();
        var vertices = new float[verts.length];
        for (int i = 0; i < verts.length / 3; i++) {
            vertices[3*i + 0] = verts[3*i + 0]*pixelSize + xPixel*pixelSize;
            vertices[3*i + 1] = verts[3*i + 1]*pixelSize + yPixel*pixelSize; // flip y
            vertices[3*i + 2] = verts[3*i + 2]*pixelSize;
        }

        var tex = templateBlockFace.getTextureCoords();
        var texCoords = new float[tex.length];
        for (int i = 0; i < tex.length / 3; i++) {
            // always the size of 1 pixel so always sample same pixel
            texCoords[3*i + 0] = (xPixel + 0.5f)*pixelSize;
            texCoords[3*i + 1] = 1f - ((yPixel + 0.5f)*pixelSize); // flip y
            texCoords[3*i + 2] = index;
        }

        return new BlockFace(index, direction, BlockFace.NO_FLAG) {
            public float[] getVertices() { return vertices; }
            public float[] getTextureCoords() { return texCoords; }
            public boolean isTransparent() { return false; }
        };
    }

}
