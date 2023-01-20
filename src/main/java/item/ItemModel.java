package item;

import render.Model;
import render.Texture;
import shader.Shader;

public class ItemModel {

    private static Shader itemShader;
    private static Texture blockTexture, itemTexture;

    public static void init(Shader _itemShader, Texture _blockTexture, Texture _itemTexture) {
        itemShader = _itemShader;
        blockTexture = _blockTexture;
        itemTexture = _itemTexture;
    }

    public static Model noModel() {
        return new Model()
                .addPosition3D(new float[0])
                .addTextureCoords3D(new float[0])
                .setTexture(blockTexture)
                .setShader(itemShader)
                .end();
    }

    // idx=texture index (z coordinate)
    public static Model cubeModel(int idx) {
        float[] vertices = new float[] {
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,

            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,

            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,

            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,

            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,

            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f
        };

        float[] textureCoords = new float[] {
            1f, 0f, idx,
            1f, 1f, idx,
            0f, 1f, idx,
            0f, 1f, idx,
            0f, 0f, idx,
            1f, 0f, idx,

            0f, 1f, idx,
            1f, 1f, idx,
            1f, 0f, idx,
            1f, 0f, idx,
            0f, 0f, idx,
            0f, 1f, idx,

            0f, 0f, idx,
            1f, 0f, idx,
            1f, 1f, idx,
            1f, 1f, idx,
            0f, 1f, idx,
            0f, 0f, idx,

            1f, 1f, idx,
            1f, 0f, idx,
            0f, 0f, idx,
            0f, 0f, idx,
            0f, 1f, idx,
            1f, 1f, idx,

            0f, 1f, idx,
            1f, 1f, idx,
            1f, 0f, idx,
            1f, 0f, idx,
            0f, 0f, idx,
            0f, 1f, idx,

            0f, 1f, idx,
            1f, 1f, idx,
            1f, 0f, idx,
            1f, 0f, idx,
            0f, 0f, idx,
            0f, 1f, idx,
        };

        return new Model()
                .addPosition3D(vertices)
                .addTextureCoords3D(textureCoords)
                .setShader(itemShader)
                .setTexture(blockTexture)
                .end();
    }

}
