package entity;

import chunk.Chunk;
import main.Keyboard;
import main.Timer;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class CameraController extends Component {

    private static final float SPEED = 0.01f;

    @Override public void start() {}
    @Override public void stop() {}
    @Override public void apply(Entity camera) {
        var transform = EntityManager.getComponent(camera, TransformationComponent.class);

        var move = SPEED * Timer.getFrametimeMillis();

        Vector3f delta = new Vector3f(0, 0, 0);
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_W)) delta.z += move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_S)) delta.z -= move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_A)) delta.x += move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_D)) delta.x -= move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE))      delta.y += move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) delta.y -= move;

        var prev = transform.getPosition();
        transform.setPosition(delta.add(prev)); // -1 0 0
    }

}
