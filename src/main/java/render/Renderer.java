package render;

import entity.Entity;
import entity.EntityManager;
import entity.ModelComponent;
import entity.TransformationComponent;
import org.lwjgl.opengl.GL30;
import shader.Shader;

import java.util.ArrayList;
import java.util.HashMap;

public class Renderer {

    public Renderer() {}

    private void prepare() {
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        GL30.glEnable(GL30.GL_DEPTH_TEST);
        EntityManager.updateComponents(TransformationComponent.class); // calc all transformation matrices
    }

    public void render(Camera camera) {
        prepare();
        var viewMatrix = camera.getViewMatrix();
        var projectionMatrix = camera.getProjectionMatrix();

        // combine so that we have a map from 1 model to multiple entities
        var map = new HashMap<Model, ArrayList<Entity>>();
        for (var entry : EntityManager.getComponents(ModelComponent.class).entrySet()) {
            var entity = entry.getKey();
            var model = entry.getValue().getModel();
            var modelEntities = map.get(model);

            if (modelEntities == null) {
                modelEntities = new ArrayList<>();
                map.put(model, modelEntities);
            }
            modelEntities.add(entity);
        }

        // render
        for (var entry : map.entrySet()) {
            var model = entry.getKey();
            var entities = entry.getValue();

            // start shader, load matrices
            GL30.glUseProgram(model.getShader().getProgram());
            model.getShader().setMatrix4f("projectionMatrix", projectionMatrix);
            model.getShader().setMatrix4f("viewMatrix", viewMatrix);

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
