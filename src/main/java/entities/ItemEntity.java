package entities;

import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import item.ItemComponent;
import item.ItemModelComponent;
import item.ItemType;
import org.joml.Vector3f;
import player.PhysicsObjectComponent;

public class ItemEntity extends Entity {

    private static final float ITEM_SIZE = 0.20f;

    public ItemEntity(Vector3f position, int itemID) {
        super();

        EntityManager.addComponent(this, new TransformationComponent(
                position,
                new Vector3f(),
                new Vector3f(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE)
        ));

        var itemSpec = ItemType.getByID(itemID);
        EntityManager.addComponent(this, new ItemModelComponent(itemSpec.getModel()));
        EntityManager.addComponent(this, new ItemComponent(itemID));
        EntityManager.addComponent(this, new PhysicsObjectComponent(new Vector3f(0.05f, 0.05f, 0.05f)));
    }

}
