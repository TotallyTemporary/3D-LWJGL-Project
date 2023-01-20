package item;

import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;
import player.PhysicsObjectComponent;
import render.Model;

public enum ItemType {

    INVALID(0, ItemModel.noModel()),
    DIRT(1,    ItemModel.cubeModel(2));

    private static final ItemType[] vals = new ItemType[4096];
    static {
        for (var item : ItemType.values()) {
            vals[item.getID()] = item;
        }
    }

    private static final float ITEM_SIZE = 0.20f;

    private int id;
    private Model model;

    ItemType(int id, Model model) {
        this.id = id;
        this.model = model;
    }

    public int getID() {
        return id;
    }

    public Model getModel() {
        return model;
    }

    public static ItemType getByID(int id) {
        return vals[id];
    }

    public static Entity makeItem(Vector3i intPos, int itemID) {
        var item = new Entity();

        var position = new Vector3f(intPos.x + 0.5f, intPos.y + 0.5f, intPos.z + 0.5f);
        var rotation = new Vector3f();
        EntityManager.addComponent(item, new TransformationComponent(
                position,
                rotation,
                new Vector3f(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE)
        ));

        var itemSpec = ItemType.getByID(itemID);
        EntityManager.addComponent(item, new ItemModelComponent(itemSpec.getModel()));
        EntityManager.addComponent(item, new ItemComponent());
        EntityManager.addComponent(item, new PhysicsObjectComponent(new Vector3f(0.05f, 0.05f, 0.05f)));

        return item;
    }

}
