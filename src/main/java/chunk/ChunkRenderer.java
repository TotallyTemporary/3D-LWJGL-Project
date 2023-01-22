package chunk;

import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import render.Texture;

import java.util.HashMap;

public class ChunkRenderer {

    /* Chunks have some special optimizations done, so they require their own render functions. */

    public static int render(HashMap<Entity, ChunkModelComponent> chunks, Matrix4f viewMatrix, Matrix4f projectionMatrix, Vector3f cameraPos) {
        int vertexTally = 0; // return number of vertices rendered (for cool statistical purposes)

        // assumptions:
        // all chunks have the same shader,
        // all chunks have the same textures
        if (chunks.size() == 0) return 0;
        var firstChunk = chunks.values().iterator().next();
        var shader = firstChunk.getModel().getShader();
        var textures = firstChunk.getModel().getTextures();

        GL30.glUseProgram(shader.getProgram());
        shader.setMatrix4f("projectionMatrix", projectionMatrix);
        shader.setMatrix4f("viewMatrix", viewMatrix);

        // bind textures
        int unit = 0;
        for (Texture tex : textures) {
            GL30.glActiveTexture(GL30.GL_TEXTURE0 + unit);
            GL30.glBindTexture(tex.getType(), tex.getTexture());
            shader.setInt(tex.getName(), unit);

            unit++;
        }

        // render chunks in this loop
        for (var entry : chunks.entrySet()) {
            var modelComp = entry.getValue();
            var entity = entry.getKey();

            // bind model and enable attribute arrays TODO: EnableVertexAttribArray actually modifies VAO, set these when calling .end() on model.
            GL30.glBindVertexArray(modelComp.getModel().getVAO());
            for (int i = 0; i < modelComp.getModel().getNumberOfVBOs(); i++)
                GL30.glEnableVertexAttribArray(i);

            var transform = EntityManager.getComponent(entity, TransformationComponent.class);
            var transMatrix = transform.getTransformationMatrix();
            shader.setMatrix4f("transformationMatrix", transMatrix);

            // the model data is partitioned to different direction faces, so we can selectively render.
            // Example: if player is clearly above a chunk, we don't need to render the BOTTOM faces.
            var chunkPos = transform.getPosition();
            if (cameraPos.y >= chunkPos.y)               vertexTally += renderFace(modelComp, CardinalDirection.UP);
            if (cameraPos.y <  chunkPos.y + Chunk.SIZE)  vertexTally += renderFace(modelComp, CardinalDirection.DOWN);
            if (cameraPos.x >= chunkPos.x)               vertexTally += renderFace(modelComp, CardinalDirection.RIGHT);
            if (cameraPos.x <  chunkPos.x + Chunk.SIZE)  vertexTally += renderFace(modelComp, CardinalDirection.LEFT);
            if (cameraPos.z >= chunkPos.z)               vertexTally += renderFace(modelComp, CardinalDirection.BACK);
            if (cameraPos.z <  chunkPos.z + Chunk.SIZE)  vertexTally += renderFace(modelComp, CardinalDirection.FRONT);

            // disable everything
            for (int i = 0; i < modelComp.getModel().getNumberOfVBOs(); i++)
                GL30.glDisableVertexAttribArray(i);
            GL30.glBindVertexArray(0);
        }

        GL30.glUseProgram(0);
        return vertexTally;
    }

    private static int renderFace(ChunkModelComponent modelComp, int face) {
        int start = modelComp.getPositionIndex(face);
        int end = modelComp.getPositionIndex(face+1);
        int count = end-start;

        if (count == 0) return 0;
        GL30.glDrawArrays(GL30.GL_TRIANGLES, start, count);
        return count;
    }
}
