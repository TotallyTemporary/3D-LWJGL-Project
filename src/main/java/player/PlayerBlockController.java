package player;

import chunk.Block;
import chunk.CardinalDirection;
import chunk.ChunkLoader;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import item.ItemType;
import main.AABB;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import render.Camera;

public class PlayerBlockController extends Component {

    private static final int MAX_DISTANCE = 10;

    private static Vector3i beforeHitLocation = null;
    private static Vector3i hitLocation = null;

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

        var rayCastData = Raycast.raycast(eyePos, lookVector, MAX_DISTANCE);
        if (rayCastData != null) {
            beforeHitLocation = rayCastData.beforeHitBlock;
            hitLocation = rayCastData.hitBlock;
        } else {
            beforeHitLocation = null;
            hitLocation = null;
        }
    }

    public void onBuildClicked(Camera player) {
        if (beforeHitLocation == null) return;

        // TODO this still spoils the chunk
        ChunkLoader.setBlockAt(beforeHitLocation, Block.COBBLESTONE.getID());
        if (isInsideBlock(player)) {
            ChunkLoader.setBlockAt(beforeHitLocation, Block.AIR.getID());
        } else {
            ChunkLoader.updateSpoiled();
        }
    }

    public void onBreakClicked(Camera player) {
        if (hitLocation == null) return;

        ChunkLoader.setBlockAt(hitLocation, Block.AIR.getID());
        ChunkLoader.updateSpoiled();

        ItemType.makeItem(hitLocation, ItemType.DIRT.getID());
    }

    private boolean isInsideBlock(Camera player) {
        var transform = EntityManager.getComponent(player, TransformationComponent.class);
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

    /*
    *                 () -> {
                    var blockController = EntityManager.getComponent(camera, PlayerBlockController.class);
                    Vector3i pos;
                    if ((pos = blockController.getHitBlock()) != null) {
                        ChunkLoader.setBlockAt(pos, Block.AIR.getID());
                        ItemType.makeItem(pos, ItemType.DIRT.getID());
                        ChunkLoader.updateSpoiled();
                    }
                },
                () -> {
                    var blockController = EntityManager.getComponent(camera, PlayerBlockController.class);
                    Vector3i pos;
                    if ((pos = blockController.getBeforeHitBlock()) != null) {
                        ChunkLoader.setBlockAt(pos, Block.COBBLESTONE.getID());
                        ChunkLoader.updateSpoiled();
                    }
                });*/
}
