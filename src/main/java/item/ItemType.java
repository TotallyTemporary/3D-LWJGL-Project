package item;

import chunk.Block;
import chunk.BlockFace;
import chunk.SquareBlockFace;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;
import player.PhysicsObjectComponent;
import render.Model;

public enum ItemType {

    INVALID  (1, ItemModel.noModel()),
    AIR      (0, ItemModel.fromBlock(Block.AIR)),
    GRASS    (2, ItemModel.fromBlock(Block.GRASS)),
    STONE    (3, ItemModel.fromBlock(Block.STONE)),
    DIRT     (4, ItemModel.fromBlock(Block.DIRT)),
    OAK_PLANK(5, ItemModel.fromBlock(Block.OAK_PLANK)),
    STONE_SLABS           (6, ItemModel.fromBlock(Block.STONE_SLABS)),
    CHISELLED_STONE_BRICKS(7, ItemModel.fromBlock(Block.CHISELLED_STONE_BRICKS)),
    BRICKS     (8, ItemModel.fromBlock(Block.BRICKS)),
    TNT        (9, ItemModel.fromBlock(Block.TNT)),
    COBWEB     (11, ItemModel.fromBlock(Block.COBWEB)),
    ROSE       (12, ItemModel.fromItemTexture(Block.ROSE)),
    DANDELION  (13, ItemModel.fromItemTexture(Block.DANDELION)),
    OAK_SAPLING(15, ItemModel.fromBlock(Block.OAK_SAPLING)),

    COBBLESTONE(16, ItemModel.fromBlock(Block.COBBLESTONE)),
    BEDROCK    (17, ItemModel.fromBlock(Block.BEDROCK)),
    SAND       (18, ItemModel.fromBlock(Block.SAND)),
    GRAVEL     (19, ItemModel.fromBlock(Block.GRAVEL)),
    OAK_LOG    (20, ItemModel.fromBlock(Block.OAK_LOG)),
    IRON_BLOCK (21, ItemModel.fromBlock(Block.IRON_BLOCK)),
    GOLD_BLOCK (22, ItemModel.fromBlock(Block.GOLD_BLOCK)),

    GOLD_ORE  (32, ItemModel.fromBlock(Block.GOLD_ORE)),
    IRON_ORE  (33, ItemModel.fromBlock(Block.IRON_ORE)),
    COAL_ORE  (34, ItemModel.fromBlock(Block.COAL_ORE)),
    BOOKSHELF (35, ItemModel.fromBlock(Block.BOOKSHELF)),
    MOSSY_COBBLESTONE(36, ItemModel.fromBlock(Block.MOSSY_COBBLESTONE)),
    OBSIDIAN  (37, ItemModel.fromBlock(Block.OBSIDIAN)),

    SPONGE      (48, ItemModel.fromBlock(Block.SPONGE)),
    GLASS       (49, ItemModel.fromBlock(Block.GLASS)),
    DIAMOND_ORE (50, ItemModel.fromBlock(Block.DIAMOND_ORE)),
    REDSTONE_ORE(51, ItemModel.fromBlock(Block.REDSTONE_ORE)),
    OAK_LEAVES(53, ItemModel.fromBlock(Block.OAK_LEAVES)),
    STONE_BRICKS (54, ItemModel.fromBlock(Block.STONE_BRICKS));

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
