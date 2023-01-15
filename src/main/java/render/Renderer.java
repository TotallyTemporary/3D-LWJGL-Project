package render;

import entity.Entity;
import entity.EntityManager;
import entity.ModelComponent;
import entity.TransformationComponent;
import org.lwjgl.opengl.GL30;
import java.util.stream.Collectors;

public class Renderer {

    public Renderer() {}

    private void prepare() {
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        GL30.glEnable(GL30.GL_DEPTH_TEST);
    }

    public void render(Camera camera) {
        prepare();
        var viewMatrix = camera.getViewMatrix();
        var projectionMatrix = camera.getProjectionMatrix();

        // 1 model for multiple entities
        var modelMap = EntityManager.getComponents(ModelComponent.class)
                .keySet().stream()
                .collect(Collectors.groupingBy(
                        entity -> EntityManager.getComponent(entity, ModelComponent.class).getModel()
                ));

        // 1 shader for multiple models
        var shaderMap = modelMap.keySet().stream()
                .collect(Collectors.groupingBy(model -> model.getShader()));


        // render
        for (var shaderEntry : shaderMap.entrySet()) {
            var shader = shaderEntry.getKey();
            var models = shaderEntry.getValue();

            // start shader, load matrices
            GL30.glUseProgram(shader.getProgram());
            shader.setMatrix4f("projectionMatrix", projectionMatrix);
            shader.setMatrix4f("viewMatrix", viewMatrix);

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
                    render(entity, model);
                }


                // disable everything
                for (int i = 0; i < model.getNumberOfVBOs(); i++)
                    GL30.glDisableVertexAttribArray(i);
                GL30.glBindVertexArray(0);
            }
            // disable shader
            GL30.glUseProgram(0);
        }
    }

    private void render(Entity entity, Model model) {
        var transformationMatrix = EntityManager
                .getComponent(entity, TransformationComponent.class)
                .getTransformationMatrix();
        model.getShader().setMatrix4f("transformationMatrix", transformationMatrix);
        if (model.hasIndexBuffer()) {
            GL30.glDrawElements(GL30.GL_TRIANGLES, model.getVertexCount(), GL30.GL_UNSIGNED_INT, 0);
        } else {
            GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, model.getVertexCount());
        }
    }

}
