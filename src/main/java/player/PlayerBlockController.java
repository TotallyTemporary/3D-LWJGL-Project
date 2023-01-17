package player;

import chunk.Block;
import chunk.ChunkLoader;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class PlayerBlockController extends Component {

    private static final int MAX_DISTANCE = 50;
    private static Vector3f lastLocation = null;

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void apply(Entity entity) {
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        assert transform != null;

        var playerPos = transform.getPosition();
        var playerRot = transform.getRotation();

        var eyePos = new Vector3f(playerPos.x,
                                  playerPos.y + PlayerMovementController.EYE_LEVEL,
                                  playerPos.z);

        Vector3f lookVector = new Vector3f(0, 0, -1)
                .rotateX(playerRot.x)
                .rotateY(playerRot.y)
                .normalize();

        lastLocation = getBlockIntersect(eyePos, lookVector);
    }

    public Vector3f getCurrentBlock() {
        return lastLocation;
    }

    private Vector3f getBlockIntersect(Vector3f pos, Vector3f moveDirection) {
        Vector3f result = new Vector3f();
        for (int step = 0; step < MAX_DISTANCE; step++) {
            result.set(0);
            moveDirection.mul(step, step, step, result);
            pos.add(result, result);

            // block = start + step * dir
            if (Block.getBlock(ChunkLoader.getBlockAt(result)).isSolid()) {
                return result;
            }
        }

        return null;
    }
}
