package block;

import entity.*;
import org.joml.Vector3f;
import org.joml.Vector3i;
import render.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class BlockEntity extends Entity implements SerializableEntity {

    public BlockEntity() {
        // only to call `deserialize()` right after
        this(new Vector3i());
    }

    public BlockEntity(Vector3i location) {
        super();
        EntityManager.addComponent(this, new BlockEntityComponent());
        EntityManager.addComponent(this, new SerializableEntityComponent());
        EntityManager.addComponent(this, new TransformationComponent(
            new Vector3f(location),
            new Vector3f(),
            new Vector3f(1)
        ));
    }

    public abstract void onBreakBlock();
    public abstract void onPlaceBlock();
    public abstract void onInteractBlock(Player player);

    // static methods
    public static void onBreakBlock(Vector3i location, Block block) {
        if (!block.hasAttachedBlockEntity()) {
            return;
        }

        BlockEntity entity = findEntityAt(location);
        if (entity != null) {
            entity.onBreakBlock();
            EntityManager.removeEntitySafe(entity);
        }
    }

    public static void onPlaceBlock(Vector3i location, Block block) {
        if (!block.hasAttachedBlockEntity()) {
            return;
        }

        block.createBlockEntity(location);
        BlockEntity entity = findEntityAt(location);
        if (entity != null) {
            entity.onPlaceBlock();
        }
    }

    public static void onInteractBlock(Player player, Vector3i location, Block block) {
        if (!block.hasAttachedBlockEntity()) {
            return;
        }

        BlockEntity entity = findEntityAt(location);
        if (entity != null) {
            entity.onInteractBlock(player);
        }
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        // write position
        var transform = EntityManager.getComponent(this, TransformationComponent.class);
        transform.serialize(out);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        // read position
        var transform = new TransformationComponent();
        transform.deserialize(in);
        EntityManager.addComponent(this, transform);
    }

    public static BlockEntity findEntityAt(Vector3i location) {
        var entities = EntityManager.getComponents(BlockEntityComponent.class).keySet();
        Vector3f floatLocation = new Vector3f(location);
        for (var entity : entities) {
            var transform = EntityManager.getComponent(entity, TransformationComponent.class);
            if (transform != null && transform.getPosition().equals(floatLocation)) {
                return (BlockEntity) entity;
            }
        }
        return null;
    }

}
