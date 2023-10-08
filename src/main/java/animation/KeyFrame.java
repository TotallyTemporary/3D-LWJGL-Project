package animation;

import org.joml.Matrix4f;

public class KeyFrame {

    private Matrix4f transformation;
    private float time;

    public KeyFrame(float time, Matrix4f transformationMatrix) {
        this.transformation = transformationMatrix;
        this.time = time;
    }

    public float getTime() {
        return time;
    }

    public Matrix4f getTransformation() {
        return transformation;
    }


}
