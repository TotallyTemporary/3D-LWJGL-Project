package ui;

import entity.ModelComponent;
import main.Display;
import render.Model;
import render.Texture;
import shader.Shader;

public class UIModelComponent extends ModelComponent {

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
            .setShader(uiShader);
    }
    private static Shader uiShader = new Shader(
            "src/main/resources/shaders/ui_vertex.glsl",
            "src/main/resources/shaders/ui_fragment.glsl"
    )
            .addUniform("transformationMatrix");

    private static Model halfHeightSquare = null;
    public UIModelComponent(Texture texture) {
        super(halfHeightSquare);
        halfHeightSquare.setTexture(texture);
        halfHeightSquare.end();
    }
}
