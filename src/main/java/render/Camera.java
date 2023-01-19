package render;

import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import player.PlayerMovementController;

public class Camera extends Entity {

    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f viewMatrix       = new Matrix4f();

    public Camera(float FOV, float aspect, float near, float far) {
        calcProjectionMatrix(FOV, aspect, near, far);
    }

    public Matrix4f getViewMatrix() {
        calcViewMatrix();
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Vector3f getEyePosition() {
        var transform = EntityManager.getComponent(this, TransformationComponent.class);
        assert transform != null;
        return new Vector3f(transform.getPosition().x,
                            transform.getPosition().y + PlayerMovementController.EYE_LEVEL,
                            transform.getPosition().z);
    }

    private void calcProjectionMatrix(float fov, float aspect, float near, float far) {
        projectionMatrix.setPerspective(fov, aspect, near, far);
    }

    private void calcViewMatrix() {
        var transform = EntityManager.getComponent(this, TransformationComponent.class);
        assert transform != null;
        var playerOrigin = transform.getPosition();
        var pos = new Vector3f(playerOrigin.x,
                               playerOrigin.y + PlayerMovementController.EYE_LEVEL,
                               playerOrigin.z);
        // view matrix basically does the inverse of a transformation matrix.
        viewMatrix.identity()
                .rotate(-transform.getRotation().x, new Vector3f(1, 0, 0))
                .rotate(-transform.getRotation().y, new Vector3f(0, 1, 0))
                .translate(-pos.x, -pos.y, -pos.z);
    }

}
