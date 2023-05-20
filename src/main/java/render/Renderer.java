package render;

import chunk.ChunkModelComponent;
import chunk.ChunkRenderer;
import entity.*;
import item.ItemModelComponent;
import org.lwjgl.opengl.GL30;
import player.BlockBreakModelComponent;
import ui.UIModelComponent;

import java.util.stream.Collectors;

/** This class works to render all the renderable objects in a scene.
 * rendering a particular model has a generic path,
 * but some objects (chunks and particles for example) require custom rendering code
 * */
public class Renderer {

    public Renderer() {}

    /** Renders the entire scene.
     * @param player This entity should be the camera.
     *               It is used to get the viewMatrix and cull some faces in the chunk render function. */
    public int render(Player player) {
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        GL30.glCullFace(GL30.GL_BACK);
        GL30.glEnable(GL30.GL_CULL_FACE);

        int vertexTally = 0; // count number of vertices rendered

        // first render terrain and items
        GL30.glEnable(GL30.GL_DEPTH_TEST);
        vertexTally += renderChunks(player);
        vertexTally += render(player, ItemModelComponent.class);
        vertexTally += render(player, BlockBreakModelComponent.class);

        // then render ui on top of everything
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        GL30.glDisable(GL30.GL_DEPTH_TEST);
        vertexTally += render(player, UIModelComponent.class);
        GL30.glDisable(GL30.GL_BLEND);

        return vertexTally;
    }

    private int renderChunks(Player player) {
        var viewMatrix = player.getViewMatrix();
        var projectionMatrix = player.getProjectionMatrix();

        var chunks = EntityManager.getComponents(ChunkModelComponent.class);
        return ChunkRenderer.render(chunks, viewMatrix, projectionMatrix, player.getEyePosition());
    }

    private <T extends ModelComponent> int render(Player player, Class<T> modelClass) {
        int vertexTally = 0;

        var viewMatrix = player.getViewMatrix();
        var projectionMatrix = player.getProjectionMatrix();

        // 1 model for multiple entities
        var modelMap = EntityManager.getComponents(modelClass)
                .keySet().stream()
                .collect(Collectors.groupingBy(
                    entity -> EntityManager.getComponent(entity, modelClass).getModel()
                ));

        // 1 shader for multiple models
        var shaderMap = modelMap.keySet().stream()
            .collect(Collectors.groupingBy(Model::getShader));

        // render
        for (var shaderEntry : shaderMap.entrySet()) {
            var shader = shaderEntry.getKey();
            var models = shaderEntry.getValue();

            // start shader, load matrices
            GL30.glUseProgram(shader.getProgram());
            if (shader.hasUniform("projectionMatrix")) shader.setMatrix4f("projectionMatrix", projectionMatrix);
            if (shader.hasUniform("viewMatrix")) shader.setMatrix4f("viewMatrix", viewMatrix);

            for (var model : models) {
                var entities = modelMap.get(model);

                // bind textures
                int unit = 0;
                for (Texture tex : model.getTextures()) {
                    GL30.glActiveTexture(GL30.GL_TEXTURE0 + unit);
                    GL30.glBindTexture(tex.getType(), tex.getTexture());
                    model.getShader().setInt(tex.getName(), unit);

                    unit++;
                }

                GL30.glBindVertexArray(model.getVAO()); // bind model, activate vbos
                for (int i = 0; i < model.getNumberOfVBOs(); i++)
                    GL30.glEnableVertexAttribArray(i);


                for (var entity : entities) {
                    vertexTally += render(entity, model);
                }


                // disable everything
                for (int i = 0; i < model.getNumberOfVBOs(); i++)
                    GL30.glDisableVertexAttribArray(i);
                GL30.glBindVertexArray(0);
            }
            // disable shader
            GL30.glUseProgram(0);
        }
        return vertexTally;
    }

    private int render(Entity entity, Model model) {
        // load possible transform
        var transformComponent = EntityManager
                .getComponent(entity, TransformationComponent.class);
        if (transformComponent != null) {
            model.getShader().setMatrix4f("transformationMatrix", transformComponent.getTransformationMatrix());
        }

        // TODO figure out a way to do this more elegantly

        // load possible ui texture index
        var uiComponent = EntityManager.getComponent(entity, UIModelComponent.class);
        if (uiComponent != null) {
            model.getShader().setFloat("uiIndex", uiComponent.getTextureIndex());
        }

        // load possible block break index
        var breakComponent = EntityManager.getComponent(entity, BlockBreakModelComponent.class);
        if (breakComponent != null) {
            model.getShader().setInt("textureIndex", breakComponent.getIndex());
        }

        if (model.hasIndexBuffer()) {
            GL30.glDrawElements(GL30.GL_TRIANGLES, model.getVertexCount(), GL30.GL_UNSIGNED_INT, 0);
        } else {
            GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, model.getVertexCount());
        }
        return model.getVertexCount();
    }

}
