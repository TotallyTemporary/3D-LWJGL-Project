package block;

import entity.Entity;
import entity.EntityManager;
import org.joml.Vector3i;
import render.Player;

import java.util.HashMap;

public abstract class BlockEntity extends Entity {

    private static final HashMap<Vector3i, BlockEntity> allBlockEntities = new HashMap<>();

    private final Vector3i location;
    private final Block block;

    public BlockEntity(Vector3i location, Block block) {
        super();
        this.location = location;
        this.block = block;
        allBlockEntities.put(location, this);
        EntityManager.addComponent(this, new BlockEntityComponent());
    }

    public Vector3i getLocation() {
        return location;
    }

    public Block getBlock() {
        return block;
    }

    public abstract void onBreakBlock();
    public abstract void onPlaceBlock();
    public abstract void onInteractBlock(Player player);

    // static methods
    public static void onBreakBlock(Vector3i location, Block block) {
        if (!block.hasAttachedBlockEntity()) {
            return;
        }

        var blockEntity = allBlockEntities.get(location);
        if (blockEntity != null) {
            blockEntity.onBreakBlock();
            EntityManager.removeEntitySafe(blockEntity);
        }
    }

    public static void onPlaceBlock(Vector3i location, Block block) {
        if (!block.hasAttachedBlockEntity()) {
            return;
        }

        block.createBlockEntity(location);
        allBlockEntities.get(location).onPlaceBlock();
    }

    public static void onInteractBlock(Player player, Vector3i location, Block block) {
        if (!block.hasAttachedBlockEntity()) {
            return;
        }

        var blockEntity = allBlockEntities.get(location);
        if (blockEntity != null) {
            blockEntity.onInteractBlock(player);
        }
    }

    public static BlockEntity getBlockEntityAt(Vector3i position) {
        return allBlockEntities.get(position);
    }

    protected void removeSelf() {
        allBlockEntities.remove(this.location);
    }

}
