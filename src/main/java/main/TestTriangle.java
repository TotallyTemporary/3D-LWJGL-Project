package main;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import shader.Shader;

import java.nio.FloatBuffer;

public class TestTriangle {

    public static float[] vertices = {
        -0.5f, -0.5f, 0.0f,
         0.5f, -0.5f, 0.0f,
         0.0f,  0.5f, 0.0f
    };

    private int vao;

    public TestTriangle() {
        // make a vao to store all the information (just 1 vbo for now)
        // the vao might be considered our "model"
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao); // binding it while making our vbos will bind the vbos to the vao.

        // create vbo to store location information
        int vbo = GL30.glGenBuffers();

        // store vertices in vbo
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buf = BufferUtils.createFloatBuffer(vertices.length);
        buf.put(vertices);
        buf.flip();
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buf, GL30.GL_STATIC_DRAW);

        // tell opengl how to interpret this data
        // index = 0 because this is the first location (adding texture coords or something will be at index 1, 2...)
        // size  = 3 because there are 3 items of data per vertex (vector3F)
        // type = float because the data type is a float
        // normalized = false because we don't want opengl tampering with our shit :D
        // stride = 3*f, how many bytes between vertices
        // pointer = 0. where data begins
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL30.glEnableVertexAttribArray(0);
        this.vao = vao;

        // unbind everything to prevent funkiness.
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void render(Shader shader) {
        GL30.glUseProgram(shader.getProgram());
        GL30.glBindVertexArray(this.vao);
        GL30.glEnableVertexAttribArray(0); // enable pos vec3

        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 3); // drawing 3 vertices

        GL30.glDisableVertexAttribArray(0); // disable pos vec3
        GL30.glBindVertexArray(0);
        GL30.glUseProgram(0);
    }
}
