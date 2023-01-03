package entity;

import chunk.Chunk;
import main.Keyboard;
import main.Timer;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class CameraController extends Component {

    private static final float SPEED = 0.01f;
    private static final float ROT_SPEED = 0.001f;

    @Override public void start() {}
    @Override public void stop() {}
    @Override public void apply(Entity camera) {
        var transform = EntityManager.getComponent(camera, TransformationComponent.class);

        var move = SPEED * Timer.getFrametimeMillis();
        var rot = ROT_SPEED * Timer.getFrametimeMillis();

        Vector3f deltaPos = new Vector3f(0, 0, 0);
        Vector3f deltaRot = new Vector3f(0, 0, 0);
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_W)) deltaPos.z -= move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_S)) deltaPos.z += move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_A)) deltaPos.x -= move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_D)) deltaPos.x += move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE))      deltaPos.y += move;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) deltaPos.y -= move;

        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT))  deltaRot.y -= rot;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT)) deltaRot.y += rot;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_UP))    deltaRot.x -= rot;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN))  deltaRot.x += rot;

        transform.setPosition(deltaPos.add(transform.getPosition()));
        transform.setRotation(deltaRot.add(transform.getRotation()));
    }

}