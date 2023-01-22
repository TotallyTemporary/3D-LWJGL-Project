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
import org.joml.Vector3f;
import org.joml.Vector3i;
import render.Player;

/** This component gives an entity (player) the ability to break and place blocks.
 * */
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
    }

     /* TODO: Make a controls system the component can subscribe to, so it doesn't have to be called externally,
        TODO: since this breaks principles of the component system */

    /** Call this function to build a block at the location this particular entity is facing.
     *  NOTE: This does not replace blocks, it places them in the location of the last airblock before hitting a solid block. */
    public void onBuildClicked(Entity entity) {
        if (beforeHitLocation == null) return;

        // TODO this still spoils the chunk
        ChunkLoader.setBlockAt(beforeHitLocation, Block.COBBLESTONE.getID());
        if (isInsideBlock(entity)) {
            ChunkLoader.setBlockAt(beforeHitLocation, Block.AIR.getID());
        } else {
            ChunkLoader.updateSpoiled();
        }
    }

    /** Call this function to break a block at the location this entity is facing. */
    public void onBreakClicked(Entity entity) {
        if (hitLocation == null) return;

        ChunkLoader.setBlockAt(hitLocation, Block.AIR.getID());
        ChunkLoader.updateSpoiled();

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
