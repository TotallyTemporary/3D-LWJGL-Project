package ui;

import entity.ModelComponent;
import main.Display;
import render.ArrayTexture;
import render.Model;
import render.Texture;
import shader.Shader;

public class UIArrayModelComponent extends ModelComponent {

    public static void createUIModels(Display display) {
        var ratio = (float) display.getWidth() / display.getHeight();
        halfHeightSquare = new Model()
            .addPosition3D(new float[] {
                -1f / ratio, -1f, -1f,
                 1f / ratio, -1f, -1f,
                 1f / ratio,  1f, -1f,
                 1f / ratio,  1f, -1f,
                -1f / ratio,  1f, -1f,
                -1f / ratio, -1f, -1f
            })
            .addTextureCoords2D(new float[]{
                0f, 0f,
                1f, 0f,
                1f, 1f,
                1f, 1f,
                0f, 1f,
                0f, 0f,
            })
            .setTexture(uiTexture)
            .setShader(uiShader)
            .end();
    }

    private static Texture uiTexture = new ArrayTexture(
            "src/main/resources/ui_array.png",
            "arrayTexture", 16, 16, -1);

    private static Shader uiShader = new Shader(
            "src/main/resources/shaders/ui_array_vertex.glsl",
            "src/main/resources/shaders/ui_array_fragment.glsl"
    )
            .addUniform("transformationMatrix")
            .addUniform("uiIndex");

    private static Model halfHeightSquare = null;

    // instance vars below

    private int index;

    public UIArrayModelComponent(int index) {
        super(halfHeightSquare);
        this.index = index;
    }

    public int getTextureIndex() {
        return index;
    }
}
