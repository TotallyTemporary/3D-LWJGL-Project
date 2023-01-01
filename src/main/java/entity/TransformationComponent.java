package entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformationComponent extends Component {

    private Vector3f position, rotation;
    private float scale;

    private Matrix4f transformationMatrix = new Matrix4f();

    public TransformationComponent(Vector3f position, Vector3f rotation, float scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    @Override public void start() {}

    @Override public void stop() {}

    // assume this gets called right before rendering.
    @Override public void apply(Entity entity) {
        var speed = 1/100f;
        this.rotation.x += speed;
        this.rotation.y += speed;
        this.rotation.z += speed;
        calculateTransformationMatrix();
    }

    private void calculateTransformationMatrix() {
        transformationMatrix.translation(position)
                .rotate(rotation.x, new Vector3f(1, 0, 0))
                .rotate(rotation.y, new Vector3f(0, 1, 0))
                .rotate(rotation.z, new Vector3f(0, 0, 1))
                .scale(scale);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Matrix4f getTransformationMatrix() {
        return transformationMatrix;
    }
}
