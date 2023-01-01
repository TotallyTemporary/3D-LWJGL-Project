package main;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import render.Model;
import shader.Shader;

import java.nio.FloatBuffer;

public class TestTriangle {

    public static float[] vertices = {
        -0.5f, -0.5f, 0.0f,
         0.5f, -0.5f, 0.0f,
         0.0f,  0.5f, 0.0f
    };

    private Model model;

    public TestTriangle() {
        this.model = new Model().addPosition3D(vertices);
    }

    public void render(Shader shader) {
        GL30.glUseProgram(shader.getProgram());
        GL30.glBindVertexArray(model.getVAO());

        for (int i = 0; i < model.getNumberOfVBOs(); i++) {
            GL30.glEnableVertexAttribArray(i);
        }

        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, model.getVertexCount());

        for (int i = 0; i < model.getNumberOfVBOs(); i++) {
            GL30.glDisableVertexAttribArray(i);
        }

        GL30.glBindVertexArray(0);
        GL30.glUseProgram(0);
    }
}
