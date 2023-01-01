package render;

import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Matrix4f;

public class Camera extends Entity {

    private Matrix4f projectionMatrix = new Matrix4f();

    public Camera(float FOV, float aspect, float near, float far) {
        calcProjectionMatrix(FOV, aspect, near, far);
    }

    public Matrix4f getViewMatrix() {
        assert EntityManager.hasComponent(this, TransformationComponent.class);
        return new Matrix4f().identity();
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    private void calcProjectionMatrix(float fov, float aspect, float near, float far) {
        projectionMatrix.setPerspective(fov, aspect, near, far);
    }

}
