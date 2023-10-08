package player;

import block.Block;
import block.CardinalDirection;
import chunk.ChunkLoader;
import entity.*;
import item.ItemType;
import item.ToolType;
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
    private BlockSelection blockSelection;

    public PlayerBlockController() {
        blockBreakEntity = new BlockBreak();
        blockSelection = new BlockSelection();
    }

    public boolean isBreakingBlock() {
        return breakage > 0f;
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

        blockSelection.setBlock(selectedBlock);
        blockSelection.setLocation(hitLocation);

        // can't switch away from a block mid-breaking it
        if (lastUpdateHitLocation == null
            || hitLocation == null
            || !(lastUpdateHitLocation.equals(hitLocation))) {
            breakage = 0;
        }

        // find hand item
        var inventory = EntityManager.getComponent(entity, PlayerInventoryController.class);
        ItemType heldItem = inventory.getSelectedItem();

        ToolType selectedTool;
        float selectedToolSpeed;

        if (heldItem != null) {
            selectedTool = heldItem.getToolType();
            selectedToolSpeed = heldItem.getCorrectToolMultiplier();
        } else {
            selectedTool = ToolType.NONE;
            selectedToolSpeed = 1f;
        }

        if (selectedTool != selectedBlock.getCorrectBreakTool()) {
            selectedToolSpeed = 1f;
        }

        var breakTime = selectedBlock.getBreakTimeSeconds();
        breakTime /= selectedToolSpeed;
        Block breakBlock = selectedBlock;
        if ((System.currentTimeMillis() - lastActionTime) > TIME_BETWEEN_ACTIONS) {
            if (Mouse.isLeftClickDown()) {
                breakage += Timer.getFrametimeSeconds();

                if (breakage >= breakTime) {
                    breakAction(entity);
                }
            } else {
                breakage = 0;
            }


            if (Mouse.isRightClickDown()) {
                buildAction(entity);
            }
        }

        if (breakage == 0) {
            breakBlock = Block.INVALID;
        }

        // update block break visual
        blockBreakEntity.setBlock(breakBlock);
        blockBreakEntity.setBreakage(breakage / breakTime);
        blockBreakEntity.setLocation(hitLocation);
    }

    private void buildAction(Entity entity) {
        lastActionTime = System.currentTimeMillis();

        var inventory = EntityManager.getComponent(entity, PlayerInventoryController.class);
        byte blockID = inventory.getSelectedItem().getPlaceBlock().getID();

        if (blockID == Block.INVALID.getID()) {
            // can't place this item (maybe it's like a potato or something)
            return;
        }

        if (beforeHitLocation == null) return;
        // TODO this still spoils the chunk
        ChunkLoader.setBlockAt(beforeHitLocation, blockID);
        if (isInsideBlock(entity)) {
            ChunkLoader.setBlockAt(beforeHitLocation, Block.AIR.getID());
            return;
        }

        inventory.removeItem();
    }

    private void breakAction(Entity entity) {
        if (hitLocation == null) return;
        lastActionTime = System.currentTimeMillis();

        var blockID = ChunkLoader.getBlockAt(hitLocation);
        ChunkLoader.setBlockAt(hitLocation, Block.AIR.getID());

        var itemID = Block.getBlock(blockID).getItemID();
        if (itemID != ItemType.INVALID.getID()) {
            ItemType.makeItem(hitLocation, itemID);
        }
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

    @Override
    public void destroy(Entity entity) {}
}
