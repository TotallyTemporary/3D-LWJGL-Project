package player;

import chunk.Block;
import chunk.CardinalDirection;
import chunk.ChunkLoader;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import main.Timer;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class PlayerMovementController extends Component {
    // player bounds
    public static final float
        WIDTH  = 0.75f,
        DEPTH = 0.75f,
        HEIGHT = 1.75f,
        EYE_LEVEL = 1.5f; // camera offset

    public static final float
        MOVE_SPEED = 8f, // blocks/second
        JUMP_SPEED = 9f, // blocks/second
        SENSITIVITY = 1/500f; // radians per pixel

    @Override public void apply(Entity entity) {
        // get player position
        var transComp = EntityManager.getComponent(entity, TransformationComponent.class);
        var physComp = EntityManager.getComponent(entity, PhysicsObjectComponent.class);

        // get player input (desired change in position)
        Vector3f deltaPos = getInput(transComp, physComp);
        physComp.altVelocity.add(deltaPos); // TODO make this acceleration and thus less shit.
    }

    // returns position change, also updates rotation directly to the component.
    private Vector3f getInput(TransformationComponent transform,
                              PhysicsObjectComponent  physics) {
        // update rotation
        var rot = transform.getRotation();

        var rotDelta = Mouse.getCursorDelta();
        rot.x -= rotDelta.y * SENSITIVITY;
        rot.y -= rotDelta.x * SENSITIVITY;

        // update position
        float front = 0;
        float left  = 0;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_W)) front += 1;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_S)) front -= 1;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_A)) left  += 1;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_D)) left  -= 1;

        float speed = MOVE_SPEED;
        float jumpSpeed = JUMP_SPEED;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            speed = 50;
            jumpSpeed = 40;
        }

        if (physics.isGrounded() && Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
            physics.velocity.y += jumpSpeed;
        }

        float angle = transform.getRotation().y;

        var forwardVector = new Vector3f(0, 0, -front).rotateY(angle);
        var sidewayVector = new Vector3f(-left,  0, 0).rotateY((float) (angle + 2*Math.PI));
        var comb = forwardVector.add(sidewayVector);
        if (comb.equals(0, 0, 0)) return comb;
        return comb.normalize().mul(speed);
    }
}
