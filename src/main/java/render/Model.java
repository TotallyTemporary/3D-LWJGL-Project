package render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Model {

    private int vao;
    private int numberOfVBOs = 0;
    private int vertexCount = -1;

    public Model() {
        this.vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(this.vao);
    }

    public Model addPosition3D(float[] positions) {
        int attribNum = numberOfVBOs++;

        int vbo = makeVBO();
        var buf = toFloatBuffer(positions);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buf, GL30.GL_STATIC_DRAW);

        // size=dimension, stride = bytes between vertices
        GL30.glVertexAttribPointer(attribNum, 3, GL30.GL_FLOAT, false, 3 * Float.BYTES, 0);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);

        // this is a bad assumption of vertex count so only use it if we have no other choice.
        // index buffer should overwrite this.
        if (this.vertexCount == -1) {
            this.vertexCount = positions.length/3;
        }
        return this;
    }

    // this is a bit of a specialized method.
    // ebo doesn't get unbinded at the end, and it doesn't need an attribute slot.
    public Model addIndices(int[] indices) {
        // make ebo (element buffer object)
        int ebo = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, ebo);
        var buf = toIntBuffer(indices);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, buf, GL30.GL_STATIC_DRAW);

        this.vertexCount = indices.length;
        return this;
    }

    private int makeVBO() {
        int vbo = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);

        return vbo;
    }

    private FloatBuffer toFloatBuffer(float[] floats) {
        var buf = BufferUtils.createFloatBuffer(floats.length);
        buf.put(floats);
        buf.flip();
        return buf;
    }

    private IntBuffer toIntBuffer(int[] ints) {
        var buf = BufferUtils.createIntBuffer(ints.length);
        buf.put(ints);
        buf.flip();
        return buf;
    }

    public int getVAO() {
        return vao;
    }

    public int getNumberOfVBOs() {
        return numberOfVBOs;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
