package main;

import org.lwjgl.opengl.GL30;
import render.Model;
import shader.Shader;

public class TestRectangle {

    static float vertices[] = {
         0.5f,  0.5f, 0.0f,
         0.5f, -0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        -0.5f,  0.5f, 0.0f
    };

    static int indices[] = {
        0, 1, 3,
        1, 2, 3
    };

    private Model model;

    public TestRectangle() {
        this.model = new Model().addPosition3D(vertices).addIndices(indices);
    }

    public void render(Shader shader) {
        // largely copied from TestTriangle, note the drawArrays call though.
        GL30.glUseProgram(shader.getProgram());
        GL30.glBindVertexArray(model.getVAO());

        for (int i = 0; i < model.getNumberOfVBOs(); i++) {
            GL30.glEnableVertexAttribArray(i);
        }

        // GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, model.getVertexCount());

        // mode, count, type, indices .
        // indices is 0 because no offset.
        GL30.glDrawElements(GL30.GL_TRIANGLES, model.getVertexCount(), GL30.GL_UNSIGNED_INT, 0);

        for (int i = 0; i < model.getNumberOfVBOs(); i++) {
            GL30.glDisableVertexAttribArray(i);
        }

        GL30.glBindVertexArray(0);
        GL30.glUseProgram(0);
    }

}
