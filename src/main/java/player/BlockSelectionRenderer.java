package player;

import entity.Entity;
import entity.EntityManager;
import entity.ModelComponent;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import render.Model;
import render.Player;
import render.Texture;
import ui.UIModelComponent;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BlockSelectionRenderer {

    private static int render(HashMap<Entity, BlockSelectModelComponent> selections) {
        var tally = 0;
        for (var entry : selections.entrySet()) {
            var entity = entry.getKey();
            var modelComponent = entry.getValue();
            var model = modelComponent.getModel();

            GL30.glBindVertexArray(model.getVAO());
            for (int i = 0; i < model.getNumberOfVBOs(); i++) {
                GL30.glEnableVertexAttribArray(i);
            }

            var transform = EntityManager.getComponent(entity, TransformationComponent.class);
            model.getShader().setMatrix4f("transformationMatrix", transform.getTransformationMatrix());

            GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, model.getVertexCount());

            for (int i = 0; i < model.getNumberOfVBOs(); i++) {
                GL30.glDisableVertexAttribArray(i);
            }
            GL30.glBindVertexArray(0);

            tally += model.getVertexCount();
        }

        return tally;
    }

    public static int render(Player player) {
        var viewMatrix = player.getViewMatrix();
        var projectionMatrix = player.getProjectionMatrix();

        var selections = EntityManager.getComponents(BlockSelectModelComponent.class);
        if (selections.isEmpty()) return 0;
        var firstSelection = selections.values().iterator().next();
        int tally = 0;

        // assume shared shader
        var transforms = selections.keySet().stream().map(entity -> EntityManager.getComponent(entity, TransformationComponent.class)).toList();
        var shader = firstSelection.getModel().getShader();
        GL30.glUseProgram(shader.getProgram());

        shader.setMatrix4f("projectionMatrix", projectionMatrix);
        shader.setMatrix4f("viewMatrix", viewMatrix);

        // assume shared texture
        var tex = firstSelection.getModel().getTextures().get(0);
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(tex.getType(), tex.getTexture());
        shader.setInt(tex.getName(), 0);

        /* thank you: https://learnopengl.com/Advanced-OpenGL/Stencil-testing */

        // regular size, full colour
        transforms.forEach(transform -> {
            transform.setScale(new Vector3f(BlockSelection.NORMAL_SCALE));
            transform.forceRecalculate();
        });
        shader.setFloat("colourMultiplier", BlockSelection.NORMAL_DARKNESS);

        GL30.glStencilOp(GL30.GL_KEEP, GL30.GL_KEEP, GL30.GL_REPLACE);
        GL30.glStencilFunc(GL30.GL_ALWAYS, 1, 0xFF);
        GL30.glStencilMask(0xFF);

        // GL30.glDisable(GL30.GL_DEPTH_TEST);
        GL30.glEnable(GL30.GL_DEPTH_TEST);

        tally += render(selections);

        // larger, fully black
        transforms.forEach(transform -> {
            transform.setScale(new Vector3f(BlockSelection.OUTLINE_SCALE));
            transform.forceRecalculate();
        });
        shader.setFloat("colourMultiplier", BlockSelection.OUTLINE_DARKNESS);

        GL30.glStencilFunc(GL30.GL_NOTEQUAL, 1, 0xFF);
        GL30.glStencilMask(0x00);

        //GL30.glDisable(GL30.GL_DEPTH_TEST);
        GL30.glEnable(GL30.GL_DEPTH_TEST);

        tally += render(selections);

        GL30.glStencilMask(0xFF);
        GL30.glStencilFunc(GL30.GL_ALWAYS, 0, 0xFF);
        GL30.glEnable(GL30.GL_DEPTH_TEST);

        return tally;
    }
}
