package ai;

import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import player.Keyboard;
import player.PhysicsObjectComponent;
import player.PlayerInventoryController;

public class BasicAIComponent extends Component {

    private float speed;
    private float jumpSpeed;
    private long jumpInterval;

    private long lastJumpTime = -1;

    public BasicAIComponent(float speed, float jumpSpeed, long jumpInterval) {
        this.speed = speed;
        this.jumpSpeed = jumpSpeed;
        this.jumpInterval = jumpInterval;
    }

    public BasicAIComponent() {
        this(2f, 10f, 1000);
    }

    @Override
    public void apply(Entity entity) {
        var physComp = EntityManager.getComponent(entity, PhysicsObjectComponent.class);
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        Vector3f ourPosition = transform.getPosition();

        // find closest player
        var players = EntityManager.getComponents(PlayerInventoryController.class);
        float closestDistance = 999999999;
        Vector3f closestPlayerPosition = null;
        for (var entry : players.entrySet()) {
            var playerTransform = EntityManager.getComponent(entry.getKey(), TransformationComponent.class);
            Vector3f theirPosition = playerTransform.getPosition();

            float distance = ourPosition.distance(theirPosition);

            if (distance <= closestDistance) {
                closestDistance = distance;
                closestPlayerPosition = theirPosition;
            }
        }
        if (closestPlayerPosition == null) {
            System.out.println("no players");
            return;
        }

        Vector3f delta = closestPlayerPosition.sub(ourPosition, new Vector3f());
        delta.y = 0;
        delta.normalize();

        if (Float.isNaN(delta.x) || Float.isNaN(delta.y) || Float.isNaN(delta.z)) {
            delta = new Vector3f();
        }

        delta.mul(speed);

        physComp.altVelocity.add(delta); // TODO make this acceleration and thus less shit.

        long now = System.currentTimeMillis();
        if (now - lastJumpTime > jumpInterval) {
            lastJumpTime = now;
            jump(physComp);
        }

    }

    private void jump(PhysicsObjectComponent physics) {
        if (physics.isGrounded()) {
            physics.velocity.y += jumpSpeed;
        }
    }
}
