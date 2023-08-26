package ui;

import entity.ModelComponent;
import main.Display;
import render.Model;
import render.Texture;
import shader.Shader;

public class UIModelComponent extends ModelComponent {

    public static float aspectRatio = 1f;

    public static void setUISettings(Display display) {
        aspectRatio = (float) display.getWidth() / display.getHeight();
    }

    private static Shader uiShader = new Shader(
        "src/main/resources/shaders/ui_vertex.glsl",
        "src/main/resources/shaders/ui_fragment.glsl"
    ).addUniform("transformationMatrix");

    public UIModelComponent(Texture texture) {
        super(createHalfHeightSquare(texture));
    }

    private static Model createHalfHeightSquare(Texture texture) {
        return new Model()
            .addPosition3D(new float[] {
                -1f / aspectRatio, -1f, -1f,
                 1f / aspectRatio, -1f, -1f,
                 1f / aspectRatio,  1f, -1f,
                 1f / aspectRatio,  1f, -1f,
                -1f / aspectRatio,  1f, -1f,
                -1f / aspectRatio, -1f, -1f
            })
            .addTextureCoords2D(new float[]{
                0f, 0f,
                1f, 0f,
                1f, 1f,
                1f, 1f,
                0f, 1f,
                0f, 0f,
            })
            .setShader(uiShader)
            .setTexture(texture)
            .end();
    }
}
