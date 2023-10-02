package render;

import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import io.MultitextureModelComponent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;
import player.BlockBreakModelComponent;
import shader.Shader;
import ui.UIArrayModelComponent;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MultitextureModelRenderer {

    public static int render(HashMap<Entity, MultitextureModelComponent> modelComponents, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        int vertexTally = 0;

        // 1 model for multiple entities
        var modelMap = EntityManager.getComponents(MultitextureModelComponent.class)
                .entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getValue(),
                        Collectors.mapping(entry->entry.getKey(), Collectors.toList())));

        // 1 shader for multiple models
        var shaderMap = modelMap.keySet().stream()
            .collect(Collectors.groupingBy(modelComp -> modelComp.getModel().getShader()));

        // render
        for (var shaderEntry : shaderMap.entrySet()) {
            var shader = shaderEntry.getKey();
            var models = shaderEntry.getValue();

            // start shader, load matrices
            GL30.glUseProgram(shader.getProgram());
            shader.setMatrix4f("projectionMatrix", projectionMatrix);
            shader.setMatrix4f("viewMatrix", viewMatrix);

            for (var modelComp : models) {
                var entities = modelMap.get(modelComp);

                GL30.glBindVertexArray(modelComp.getModel().getVAO()); // bind model

                // render multiple times, once per texture
                for (int partIndex = 0; partIndex < modelComp.getPartCount(); partIndex++) {
                    var part = modelComp.getPart(partIndex);

                    // bind texture
                    Texture tex = part.getTexture();
                    GL30.glActiveTexture(GL30.GL_TEXTURE0);
                    GL30.glBindTexture(tex.getType(), tex.getTexture());
                    shader.setInt(tex.getName(), 0);

                    for (var entity : entities) {
                        vertexTally += render(entity, modelComp.getModel(), part.getStartVertex(), part.getVertexCount());
                    }
                }
            }
        }
        return vertexTally;
    }

    private static int render(Entity entity, Model model, int start, int count) {
        // load possible transform
        var transformComponent = EntityManager
                .getComponent(entity, TransformationComponent.class);
        if (transformComponent != null) {
            model.getShader().setMatrix4f("transformationMatrix", transformComponent.getTransformationMatrix());
        }

        GL30.glDrawArrays(GL30.GL_TRIANGLES, start, count);

        return count;
    }
}
