package render;

import chunk.ChunkModelComponent;
import chunk.ChunkRenderer;
import entity.*;
import io.MultitextureModelComponent;
import item.ItemModelComponent;
import org.lwjgl.opengl.GL30;
import player.BlockBreakModelComponent;
import player.BlockSelectionRenderer;
import ui.UIArrayModelComponent;
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
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT | GL30.GL_STENCIL_BUFFER_BIT);
        GL30.glCullFace(GL30.GL_BACK);
        GL30.glEnable(GL30.GL_CULL_FACE);
        GL30.glEnable(GL30.GL_DEPTH_TEST);

        GL30.glEnable(GL30.GL_STENCIL_TEST);
        GL30.glStencilOp(GL30.GL_KEEP, GL30.GL_KEEP, GL30.GL_REPLACE);
        GL30.glStencilMask(0x00); // disable stenciling

        int vertexTally = 0; // count number of vertices rendered

        // first render terrain and items
        vertexTally += renderChunks(player);
        vertexTally += render(player, ItemModelComponent.class);
        vertexTally += renderMultitexture(player);

        vertexTally += renderSelections(player);
        vertexTally += render(player, BlockBreakModelComponent.class);

        // then render ui on top of everything
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        GL30.glDisable(GL30.GL_DEPTH_TEST);
        vertexTally += render(player, UIArrayModelComponent.class);
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

    private int renderMultitexture(Player player) {
        var viewMatrix = player.getViewMatrix();
        var projectionMatrix = player.getProjectionMatrix();

        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

        var multitexturedModels = EntityManager.getComponents(MultitextureModelComponent.class);
        int vertexTally = MultitextureModelRenderer.render(multitexturedModels, viewMatrix, projectionMatrix);

        GL30.glDisable(GL30.GL_BLEND);

        return vertexTally;
    }

    private int renderSelections(Player player) {
        return BlockSelectionRenderer.render(player);
    }

    private <T extends ModelComponent> int render(Player player, Class<T> modelClass) {
        int vertexTally = 0;

        var viewMatrix = player.getViewMatrix();
        var projectionMatrix = player.getProjectionMatrix();

        // 1 model for multiple entities
        var modelMap = EntityManager.getComponents(modelClass)
                .entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getValue().getModel(),
                        Collectors.mapping(entry->entry.getKey(), Collectors.toList())));
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

                GL30.glBindVertexArray(model.getVAO()); // bind model

                for (var entity : entities) {
                    vertexTally += render(entity, model);
                }

            }
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
        var uiComponent = EntityManager.getComponent(entity, UIArrayModelComponent.class);
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
