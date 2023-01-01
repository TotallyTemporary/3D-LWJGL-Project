package render;

import entity.Entity;
import entity.EntityManager;
import entity.ModelComponent;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.HashMap;

public class Renderer {

    public Renderer() {}

    public void render() {
        // combine so that we have a map from 1 model to multiple entities
        var map = new HashMap<Model, ArrayList<Entity>>();
        EntityManager.getComponents(ModelComponent.class)
                .forEach((entity, component) -> {
            var model = component.getModel();
            var modelEntities = map.get(model);
            if (modelEntities == null) {
                modelEntities = new ArrayList<>();
                map.put(model, modelEntities);
            }
            modelEntities.add(entity);
        });

        // render
        map.forEach((model, entities) -> {
            // enable and bind and all that shit
            GL30.glUseProgram(model.getShader().getProgram());
            GL30.glBindVertexArray(model.getVAO());
            for (int i = 0; i < model.getNumberOfVBOs(); i++)
                GL30.glEnableVertexAttribArray(i);

            if (model.hasIndexBuffer()) {
                GL30.glDrawElements(GL30.GL_TRIANGLES, model.getVertexCount(), GL30.GL_UNSIGNED_INT, 0);
            } else {
                GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, model.getVertexCount());
            }

            // disable everything
            for (int i = 0; i < model.getNumberOfVBOs(); i++)
                GL30.glDisableVertexAttribArray(i);
            GL30.glBindVertexArray(0);
            GL30.glUseProgram(0);
        });
    }

}
