package render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import shader.Shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A model represents something that can be rendered.
 * It should be built by creating the model and calling .addSomething -methods to add VBOs.
 * Example: [var model = new Model().addPosition3D(vertices);]
 */
public class Model {
    // some static stuff to prevent memory leaks
    private static ArrayList<Integer> allVAOs = new ArrayList<>();
    private static ArrayList<Integer> allVBOs = new ArrayList<>();

    public static void destroyAll() {
        for (var vao : allVAOs) {
            GL30.glDeleteVertexArrays(vao);
        }

        for (var vbo : allVBOs) {
            GL30.glDeleteBuffers(vbo);
        }
    }

    private int vao;
    private final ArrayList<Integer> vbos = new ArrayList<>();
    private int vertexCount = -1;
    private boolean hasIndexBuffer = false;

    private Shader shader;
    private List<Texture> textures = new ArrayList<>();

    public Model() {
        this.vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(this.vao);
    }

    public Model setShader(Shader shader) {
        this.shader = shader;
        return this;
    }

    public Model setTexture(Texture texture) {
        this.textures.add(texture);
        return this;
    }

    public Model addPosition3D(float[] positions) {
        int attribNum = vbos.size();
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

    public Model addPosition2D(float[] positions) {
        int attribNum = vbos.size();
        int vbo = makeVBO();
        var buf = toFloatBuffer(positions);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buf, GL30.GL_STATIC_DRAW);

        GL30.glVertexAttribPointer(attribNum, 2, GL30.GL_FLOAT, false, 2 * Float.BYTES, 0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        if (this.vertexCount == -1) {
            this.vertexCount = positions.length/2;
        }
        return this;
    }

    public Model addTextureCoords3D(float[] textureCoords) {
        int attribNum = vbos.size();
        int vbo = makeVBO();
        var buf = toFloatBuffer(textureCoords);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buf, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(attribNum, 3, GL30.GL_FLOAT, false, 3 * Float.BYTES, 0);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        return this;
    }

    public Model addLight2D(float[] light) {
        int attribNum = vbos.size();
        int vbo = makeVBO();
        var buf = toFloatBuffer(light);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buf, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(attribNum, 2, GL30.GL_FLOAT, false, 2 * Float.BYTES, 0);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        return this;
    }

    public Model addTextureCoords2D(float[] textureCoords) {
        int attribNum = vbos.size();
        int vbo = makeVBO();
        var buf = toFloatBuffer(textureCoords);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buf, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(attribNum, 2, GL30.GL_FLOAT, false, 2 * Float.BYTES, 0);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        return this;
    }

    /* TODO can re-add when figured out how to unload this without taking an attribute slot
    // this is a bit of a specialized method.
    // ebo doesn't get unbinded at the end, and it doesn't need an attribute slot.
    public Model addIndices(int[] indices) {
        // make ebo (element buffer object)
        int ebo = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, ebo);
        var buf = toIntBuffer(indices);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, buf, GL30.GL_STATIC_DRAW);

        this.vertexCount = indices.length;
        this.hasIndexBuffer = true;
        return this;
    }
     */

    private int makeVBO() {
        int vbo = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);
        allVBOs.add(vbo);
        vbos.add(vbo);
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

    public Model end() {
        // load all the textures into the shader
        for (var texture : textures) {
            shader.addUniform(texture.name);
        }

        // enable all vertex attrib arrays
        GL30.glBindVertexArray(getVAO());
        for (int i = 0; i < getNumberOfVBOs(); i++) {
            GL30.glEnableVertexAttribArray(i);
        }

        return this;
    }

    public void destroy() {
        for (var vbo : this.vbos) {
            allVBOs.remove(vbo);
            GL30.glDeleteBuffers(vbo);
        }
        this.vbos.clear();

        allVAOs.remove((Integer) this.vao);
        GL30.glDeleteVertexArrays(vao);
        this.vao = -1;
    }

    public int getVAO() {
        return vao;
    }

    public int getVBO(int index) {
        return vbos.get(index);
    }

    public int getNumberOfVBOs() {
        return vbos.size();
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public boolean hasIndexBuffer() {
        return hasIndexBuffer;
    }

    public Shader getShader() {
        return shader;
    }

    public List<Texture> getTextures() {
        return textures;
    }
}
