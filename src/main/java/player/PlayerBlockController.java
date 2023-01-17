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
import render.Camera;

public class PlayerBlockController extends Component {

    private static final int MAX_DISTANCE = 50;

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
        if (Math.floor(pos.x) - pos.x == 0) pos.x += 1f/10_000;
        if (Math.floor(pos.y) - pos.y == 0) pos.y += 1f/10_000;
        if (Math.floor(pos.z) - pos.z == 0) pos.z += 1f/10_000;

        Vector3i ipos = new Vector3i(pos, RoundingMode.FLOOR);

        float stepX = Math.signum(t.x);
        float stepY = Math.signum(t.y);
        float stepZ = Math.signum(t.z);

        float tMaxX = calcMaxT(pos.x, t.x);
        float tMaxY = calcMaxT(pos.y, t.y);
        float tMaxZ = calcMaxT(pos.z, t.z);

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

    private float calcMaxT(float pos, float vec) {
        if (vec > 0) return ((float) Math.ceil(pos) - pos) / Math.abs(vec);
        return -((float) Math.floor(pos) - pos) / Math.abs(vec);
    }
}
