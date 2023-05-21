package player;

import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import render.Player;

import java.util.HashMap;

public class BlockSelectionRenderer {

    private static int render(SelectedFaceModelComponent modelComponent, TransformationComponent transform) {
        var model = modelComponent.getModel();

        GL30.glBindVertexArray(model.getVAO());
        for (int i = 0; i < model.getNumberOfVBOs(); i++) {
            GL30.glEnableVertexAttribArray(i);
        }

        model.getShader().setMatrix4f("transformationMatrix", transform.getTransformationMatrix());

        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, model.getVertexCount());

        for (int i = 0; i < model.getNumberOfVBOs(); i++) {
            GL30.glDisableVertexAttribArray(i);
        }
        GL30.glBindVertexArray(0);

        return model.getVertexCount();
    }

    public static int render(Player player) {
        var viewMatrix = player.getViewMatrix();
        var projectionMatrix = player.getProjectionMatrix();

        var selections = EntityManager.getComponents(SelectedFaceModelComponent.class);
        if (selections.isEmpty()) return 0;
        var firstSelection = selections.values().iterator().next();
        int tally = 0;

        // assume shared shader
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

        for (var entity : selections.keySet()) {
            var modelComp = EntityManager.getComponent(entity, SelectedFaceModelComponent.class);
            var transformComp = EntityManager.getComponent(entity, TransformationComponent.class);

            GL30.glClear(GL30.GL_STENCIL_BUFFER_BIT); // clear stencil from last selection

            // regular size, full colour
            transformComp.setScale(new Vector3f(BlockSelection.NORMAL_SCALE));
            transformComp.forceRecalculate();
            shader.setFloat("colourMultiplier", BlockSelection.NORMAL_DARKNESS);

            // write to stencil at all times
            GL30.glStencilOp(GL30.GL_KEEP, GL30.GL_KEEP, GL30.GL_REPLACE);
            GL30.glStencilFunc(GL30.GL_ALWAYS, 1, 0xFF);
            GL30.glStencilMask(0xFF);

            // dont write colour and dont check depth
            GL30.glColorMask(false, false, false, false); // don't write colour
            GL30.glDisable(GL30.GL_DEPTH_TEST);

            tally += render(modelComp, transformComp);

            // larger, darker
            transformComp.setScale(new Vector3f(BlockSelection.OUTLINE_SCALE));
            transformComp.forceRecalculate();
            shader.setFloat("colourMultiplier", BlockSelection.OUTLINE_DARKNESS);

            // dont render a pixel if stencil was set by the render above
            GL30.glStencilFunc(GL30.GL_NOTEQUAL, 1, 0xFF);
            GL30.glStencilMask(0x00);

            // write colour, test depth
            GL30.glColorMask(true, true, true, true);
            GL30.glEnable(GL30.GL_DEPTH_TEST); // turn this to glDisable if you want outlines you can see through ground

            tally += render(modelComp, transformComp);

            // return defaults
            GL30.glStencilMask(0xFF);
            GL30.glStencilFunc(GL30.GL_ALWAYS, 0, 0xFF);
            GL30.glEnable(GL30.GL_DEPTH_TEST);
        }

        return tally;
    }
}
