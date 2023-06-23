package player;

import block.BlockFace;
import entity.ModelComponent;
import org.lwjgl.opengl.GL30;
import render.Model;
import render.Texture;
import shader.Shader;

public class SelectedFaceModelComponent extends ModelComponent {

    private static final int MAX_VERTICES_COUNT = 40 * 3; // max 40 vertices (3D)
    private static final int MAX_TEXCOORDS_COUNT = 40 * 3; // max 40 vertices (3D)

    private Texture texture;
    private Shader shader;

    private BlockFace face;

    public SelectedFaceModelComponent(Texture texture, Shader shader) {
        super(null);
        this.texture = texture;
        this.shader = shader;
        this.face = null;

        super.model = makeInitialModel();
    }

    public BlockFace getFace() {
        return this.face;
    }

    public void setFace(BlockFace face) {
        if (this.face == face) return;

        this.face = face;
        updateModel(face);
    }

    private Model makeInitialModel() {
        return new Model()
            .addPosition3D(new float[MAX_VERTICES_COUNT])
            .addTextureCoords3D(new float[MAX_TEXCOORDS_COUNT])
            .setTexture(texture)
            .setShader(shader)
            .end();
    }

    private void updateModel(BlockFace face) {
        float[] vertices = new float[MAX_VERTICES_COUNT];
        float[] texCoords = new float[MAX_TEXCOORDS_COUNT];

        if (face != null) {
            var faceVertices = face.getVertices();
            System.arraycopy(faceVertices, 0, vertices, 0, faceVertices.length);

            var faceTexCoords = face.getTextureCoords();
            System.arraycopy(faceTexCoords, 0, texCoords, 0, faceTexCoords.length);
        }

        var vertexVBO = model.getVBO(0);
        var texVBO = model.getVBO(1);

        GL30.glBindVertexArray(model.getVAO());

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vertexVBO);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertices);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, texVBO);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, texCoords);
    }
}
