package player;

import chunk.Block;
import chunk.ChunkLoader;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import render.Camera;

public class PlayerBlockController extends Component {

    private static final int MAX_DISTANCE = 50;
    private static final float SMALL_OFFSET = 0.0001f;

    private static Vector3i beforeHitLocation = null;
    private static Vector3i hitLocation = null;

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void apply(Entity entity) {
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        assert transform != null;

        var playerPos = transform.getPosition();
        var eyePos = new Vector3f(playerPos.x,
                                  playerPos.y + PlayerMovementController.EYE_LEVEL,
                                  playerPos.z);

        Camera camera = (Camera) entity;
        var iViewMatrix = camera.getViewMatrix();
        var lookVector = new Vector3f(-iViewMatrix.m02(), -iViewMatrix.m12(), -iViewMatrix.m22());

        getBlockIntersect(eyePos, lookVector);
    }

    public Vector3i getHitBlock() {
        return hitLocation;
    }

    public Vector3i getBeforeHitBlock() {
        return beforeHitLocation;
    }

    // http://www.cse.yorku.ca/~amana/research/grid.pdf
    private void getBlockIntersect(Vector3f pos, Vector3f t) {
        pos.add(SMALL_OFFSET, SMALL_OFFSET, SMALL_OFFSET); // TODO: Somehow fix raycasting mucking up when pos is integer value.

        Vector3i ipos = new Vector3i(pos, RoundingMode.FLOOR);

        float stepX = Math.signum(t.x);
        float stepY = Math.signum(t.y);
        float stepZ = Math.signum(t.z);

        float tMaxX, tMaxY, tMaxZ; // TODO separate into method
        if (t.x > 0) tMaxX = ((float) Math.ceil(pos.x) - pos.x) / Math.abs(t.x);
        else         tMaxX = (pos.x - (float) Math.floor(pos.x)) / Math.abs(t.x);

        if (t.y > 0) tMaxY = ((float) Math.ceil(pos.y) - pos.y) / Math.abs(t.y);
        else         tMaxY = (pos.y - (float) Math.floor(pos.y)) / Math.abs(t.y);

        if (t.z > 0) tMaxZ = ((float) Math.ceil(pos.z) - pos.z) / Math.abs(t.z);
        else         tMaxZ = (pos.z - (float) Math.floor(pos.z)) / Math.abs(t.z);

        float tDeltaX = stepX/t.x;
        float tDeltaY = stepY/t.y;
        float tDeltaZ = stepZ/t.z;

        Vector3i lastBlock = new Vector3i().set(ipos);
        int steps = 0;
        while (steps < MAX_DISTANCE) {
            steps++;
            if (ChunkLoader.getBlockAt(ipos) != Block.AIR.getID()) {
                hitLocation = ipos;
                beforeHitLocation = lastBlock;

                break;
            }
            lastBlock.set(ipos);

            if (Math.abs(tMaxX) < Math.abs(tMaxY)) {
                if (Math.abs(tMaxX) < Math.abs(tMaxZ)) {
                    ipos.x += stepX;
                    tMaxX += tDeltaX;
                } else {
                    ipos.z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (Math.abs(tMaxY) < Math.abs(tMaxZ)) {
                    ipos.y += stepY;
                    tMaxY += tDeltaY;
                } else {
                    ipos.z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            }
        }
    }
}
