package item;

import main.Display;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import render.BasicTexture;
import render.Model;
import render.Texture;

import java.nio.ByteBuffer;

public class ItemThumbnailRenderer {

    private static final int SIZE = 24;

    public static ItemThumbnailTexture renderItem(int itemID) {
        int[] viewportBefore = new int[4];
        GL30.glGetIntegerv(GL30.GL_VIEWPORT, viewportBefore);

        ItemType item = ItemType.getByID(itemID);
        Model itemModel = item.getModel();
        Matrix4f transformationMatrix = item.getThumbnailAlignment().getTransformationMatrix();

        int fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);

        // create colour texture
        int colourTexture = GL30.glGenTextures();
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, colourTexture);
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, SIZE, SIZE, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, colourTexture, 0);

        // attach depth buffer
        int rbo = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbo);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT, SIZE, SIZE);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, rbo);

        GL30.glEnable(GL30.GL_DEPTH_TEST);
        GL30.glDisable(GL30.GL_CULL_FACE); // TODO normals are inverted
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        GL30.glViewport(0, 0, SIZE, SIZE);

        renderItem(itemModel, transformationMatrix);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

        GL30.glDeleteFramebuffers(fbo);

        GL30.glViewport(0, 0, viewportBefore[2], viewportBefore[3]);

        return new ItemThumbnailTexture(colourTexture);
    }

    private static void renderItem(Model item, Matrix4f transformationMatrix) {
        var shader = item.getShader();
        var textures = item.getTextures();

        GL30.glUseProgram(shader.getProgram());
        GL30.glBindVertexArray(item.getVAO());

        shader.setMatrix4f("transformationMatrix", transformationMatrix);
        shader.setMatrix4f("viewMatrix", new Matrix4f().identity());
        shader.setMatrix4f("projectionMatrix", new Matrix4f().identity());

        // bind textures
        int unit = 0;
        for (Texture tex : textures) {
            GL30.glActiveTexture(GL30.GL_TEXTURE0 + unit);
            GL30.glBindTexture(tex.getType(), tex.getTexture());
            shader.setInt(tex.getName(), unit);
            unit++;
        }

        // render
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, item.getVertexCount());
    }



}
