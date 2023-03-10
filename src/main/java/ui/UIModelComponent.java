package ui;

import entity.ModelComponent;
import main.Display;
import render.ArrayTexture;
import render.Model;
import render.Texture;
import shader.Shader;

public class UIModelComponent extends ModelComponent {

    public static void createUIModels(Display display) {
        var as = (float) display.getWidth() / display.getHeight();
        halfHeightSquare = new Model()
            .addPosition3D(new float[] {
                -1f / as, -1f, -1f,
                 1f / as, -1f, -1f,
                 1f / as,  1f, -1f,
                 1f / as,  1f, -1f,
                -1f / as,  1f, -1f,
                -1f / as, -1f, -1f
            })
            .addTextureCoords2D(new float[]{
                0f, 1f,
                1f, 1f,
                1f, 0f,
                1f, 0f,
                0f, 0f,
                0f, 1f,
            })
            .setTexture(uiTexture)
            .setShader(uiShader)
            .end();
    }

    private static Texture uiTexture = new ArrayTexture(
            "src/main/resources/ui_array.png",
            "arrayTexture", 16, 16);

    private static Shader uiShader = new Shader(
            "src/main/resources/shaders/ui_vertex.glsl",
            "src/main/resources/shaders/ui_fragment.glsl"
    )
            .addUniform("transformationMatrix")
            .addUniform("uiIndex");

    private static Model halfHeightSquare = null;

    // instance vars below

    private int index;

    public UIModelComponent(int index) {
        super(halfHeightSquare);
        this.index = index;
    }

    public int getTextureIndex() {
        return index;
    }
}
