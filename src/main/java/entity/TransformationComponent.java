package entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Contains position, rotating and scale for an entity. */
public class TransformationComponent extends Component implements SerializableComponent {

    private Vector3f position, rotation, scale;

    private Matrix4f transformationMatrix = new Matrix4f();

    public TransformationComponent(Vector3f position, Vector3f rotation, Vector3f scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        calculateTransformationMatrix();
    }

    public TransformationComponent() {
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.scale = new Vector3f();
        calculateTransformationMatrix();
    }

    // assume this gets called right before rendering.
    @Override public void apply(Entity entity) {
        calculateTransformationMatrix();
    }

    public void forceRecalculate() {
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

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    /* This is a bit of hack,
    * it doesn't persist onto next frame so update has to be done before transform comp update
    * this is used by AnimatorComponent now. */
    public void doTransformation(Matrix4f transform) {
        transformationMatrix.mul(transform);
    }

    public Matrix4f getTransformationMatrix() {
        return transformationMatrix;
    }

    @Override
    public void destroy(Entity entity) {}

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeFloat(position.x);
        out.writeFloat(position.y);
        out.writeFloat(position.z);

        out.writeFloat(rotation.x);
        out.writeFloat(rotation.y);
        out.writeFloat(rotation.z);

        out.writeFloat(scale.x);
        out.writeFloat(scale.y);
        out.writeFloat(scale.z);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        position.x = in.readFloat();
        position.y = in.readFloat();
        position.z = in.readFloat();

        rotation.x = in.readFloat();
        rotation.y = in.readFloat();
        rotation.z = in.readFloat();

        scale.x = in.readFloat();
        scale.y = in.readFloat();
        scale.z = in.readFloat();

        forceRecalculate();
    }
}
