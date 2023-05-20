package player;

import chunk.Block;
import chunk.CardinalDirection;
import chunk.ChunkLoader;
import entity.*;
import item.ItemType;
import main.AABB;
import main.Timer;
import org.joml.Vector3f;
import org.joml.Vector3i;
import render.Player;

/** This component gives an entity (player) the ability to break and place blocks.
 * */
public class PlayerBlockController extends Component {

    private static final int MAX_DISTANCE = 8;
    private static final long TIME_BETWEEN_ACTIONS = 150; // millis

    private Vector3i beforeHitLocation = null;
    private Vector3i hitLocation = null;

    private Vector3i lastUpdateHitLocation = null;
    private float breakage = 0;

    private long lastActionTime = System.currentTimeMillis();

    private BlockBreak blockBreakEntity;

    public PlayerBlockController() {
        blockBreakEntity = new BlockBreak();
    }

    @Override
    public void apply(Entity entity) {
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        assert transform != null;

        lastUpdateHitLocation = hitLocation;

        var playerPos = transform.getPosition();
        var eyePos = new Vector3f(playerPos.x,
                                  playerPos.y + PlayerMovementController.EYE_LEVEL,
                                  playerPos.z);

        Player player = (Player) entity;
        var iViewMatrix = player.getViewMatrix();
        var lookVector = new Vector3f(-iViewMatrix.m02(), -iViewMatrix.m12(), -iViewMatrix.m22());

        var rayCastData = Raycast.raycast(eyePos, lookVector, MAX_DISTANCE);
        if (rayCastData != null) {
            beforeHitLocation = rayCastData.beforeHitBlock;
            hitLocation = rayCastData.hitBlock;
        } else {
            beforeHitLocation = null;
            hitLocation = null;
        }

        Block selectedBlock;
        if (hitLocation != null) {
            selectedBlock = Block.getBlock(ChunkLoader.getBlockAt(hitLocation));
        } else {
            selectedBlock = Block.INVALID;
        }

        // can't switch away from a block mid-breaking it
        if (lastUpdateHitLocation == null
            || hitLocation == null
            || !(lastUpdateHitLocation.equals(hitLocation))) {
            breakage = 0;
        }

        var breakTime = 0.5f; // TODO will be set on a per-block basis.
        Block breakBlock = selectedBlock;
        if ((System.currentTimeMillis() - lastActionTime) > TIME_BETWEEN_ACTIONS) {
            if (Mouse.isLeftClickDown()) {
                breakage += Timer.getFrametimeSeconds();

                if (breakage >= breakTime) {
                    breakAction(entity);
                    lastActionTime = System.currentTimeMillis();
                }
            } else {
                breakBlock = Block.INVALID;
                breakage = 0;
            }


            if (Mouse.isRightClickDown()) {
                buildAction(entity);
                lastActionTime = System.currentTimeMillis();
            }
        }

        // update block break visual
        blockBreakEntity.setBlock(breakBlock);
        blockBreakEntity.setBreakage(breakage / breakTime);
        blockBreakEntity.setLocation(hitLocation);
    }

    private void buildAction(Entity entity) {
        if (beforeHitLocation == null) return;
        // TODO this still spoils the chunk
        ChunkLoader.setBlockAt(beforeHitLocation, Block.COBBLESTONE.getID());
        if (isInsideBlock(entity)) {
            ChunkLoader.setBlockAt(beforeHitLocation, Block.AIR.getID());
        }
    }

    private void breakAction(Entity entity) {
        if (hitLocation == null) return;
        ChunkLoader.setBlockAt(hitLocation, Block.AIR.getID());
        ItemType.makeItem(hitLocation, ItemType.DIRT.getID());
    }

    private boolean isInsideBlock(Entity entity) {
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        var pos = transform.getPosition();

        var aabb = new AABB(pos,
                            PlayerMovementController.WIDTH,
                            PlayerMovementController.HEIGHT,
                            PlayerMovementController.DEPTH);

        for (int dir = 0; dir < CardinalDirection.COUNT; dir++) {
            for (var point : aabb.getTestPoints(dir)) {
                if (Block.getBlock(ChunkLoader.getBlockAt(point)).isSolid()) {
                    return true;
                }
            }
        }

        return false;
    }
}
