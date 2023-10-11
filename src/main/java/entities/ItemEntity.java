package entities;

import entity.*;
import item.ItemComponent;
import item.ItemModelComponent;
import item.ItemType;
import org.joml.Vector3f;
import player.PhysicsObjectComponent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ItemEntity extends Entity implements SerializableEntity {

    private static final float ITEM_SIZE = 0.20f;

    private int itemID;

    public ItemEntity() {
        this(new Vector3f(), 0);
    }

    public ItemEntity(Vector3f position, int itemID) {
        super();
        this.itemID = itemID;

        EntityManager.addComponent(this, new TransformationComponent(
                position,
                new Vector3f(),
                new Vector3f(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE)
        ));

        EntityManager.addComponent(this, new ItemModelComponent(itemID));
        EntityManager.addComponent(this, new ItemComponent(itemID));
        EntityManager.addComponent(this, new PhysicsObjectComponent(new Vector3f(0.05f, 0.05f, 0.05f)));
        EntityManager.addComponent(this, new SerializableEntityComponent());
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        // write position
        var transform = EntityManager.getComponent(this, TransformationComponent.class);
        transform.serialize(out);

        // TODO write physics component

        // write item id
        out.writeInt(itemID);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        // read transform
        var transform = new TransformationComponent();
        transform.deserialize(in);
        EntityManager.addComponent(this, transform);

        // TODO read physics component

        // read item id
        int itemID = in.readInt();

        // apply item id to components
        EntityManager.addComponent(this, new ItemModelComponent(itemID));
        EntityManager.addComponent(this, new ItemComponent(itemID));
    }

    @Override
    public int getType() {
        return EntityType.ITEM;
    }
}
