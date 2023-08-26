package item;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public enum ItemThumbnailAlignment {

    Block(
            new Matrix4f()
            .identity()
            .translate(0, -0.5f, 0.20f)
            .rotate((float) Math.toRadians(-20), new Vector3f(1, 0, 0))
            .rotate((float) Math.toRadians(45), new Vector3f(0, 1, 0))
            .scale(1.20f)
    ),
    Item(
            new Matrix4f()
                    .identity()
                    .translate(0, -0.5f, 0.20f)
    );

    private Matrix4f transformationMatrix;

    private ItemThumbnailAlignment(Matrix4f transformationMatrix) {
        this.transformationMatrix = transformationMatrix;
    }

    public Matrix4f getTransformationMatrix() {
        return transformationMatrix;
    }

}
