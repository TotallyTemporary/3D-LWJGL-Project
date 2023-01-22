package player;

import chunk.Block;
import chunk.ChunkLoader;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import item.ItemType;
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
        }
    }

    public void onBuildClicked(Camera player) {
        if (beforeHitLocation == null) return;

        ChunkLoader.setBlockAt(beforeHitLocation, Block.COBBLESTONE.getID());
        ChunkLoader.updateSpoiled();
    }

    public void onBreakClicked(Camera player) {
        if (hitLocation == null) return;

        ChunkLoader.setBlockAt(hitLocation, Block.AIR.getID());
        ChunkLoader.updateSpoiled();

        ItemType.makeItem(hitLocation, ItemType.DIRT.getID());
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
