package item;

import block.Block;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;
import player.PhysicsObjectComponent;
import render.Model;

public enum ItemType {

    INVALID  (1, ItemModel.noModel(), ItemThumbnailAlignment.Block),
    AIR      (0, ItemModel.noModel(), ItemThumbnailAlignment.Block),
    GRASS    (2, ItemModel.fromBlock(Block.GRASS), ItemThumbnailAlignment.Block),
    STONE    (3, ItemModel.fromBlock(Block.STONE), ItemThumbnailAlignment.Block),
    DIRT     (4, ItemModel.fromBlock(Block.DIRT), ItemThumbnailAlignment.Block),
    OAK_PLANK(5, ItemModel.fromBlock(Block.OAK_PLANK), ItemThumbnailAlignment.Block),
    STONE_SLABS           (6, ItemModel.fromBlock(Block.STONE_SLABS), ItemThumbnailAlignment.Block),
    CHISELLED_STONE_BRICKS(7, ItemModel.fromBlock(Block.CHISELLED_STONE_BRICKS), ItemThumbnailAlignment.Block),
    BRICKS     (8, ItemModel.fromBlock(Block.BRICKS), ItemThumbnailAlignment.Block),
    TNT        (9, ItemModel.fromBlock(Block.TNT), ItemThumbnailAlignment.Block),
    COBWEB     (11, ItemModel.fromBlock(Block.COBWEB), ItemThumbnailAlignment.Block),
    ROSE       (12, ItemModel.fromItemTexture(Block.ROSE), ItemThumbnailAlignment.Item),
    DANDELION  (13, ItemModel.fromItemTexture(Block.DANDELION), ItemThumbnailAlignment.Item),
    OAK_SAPLING(15, ItemModel.fromBlock(Block.OAK_SAPLING), ItemThumbnailAlignment.Item),

    COBBLESTONE(16, ItemModel.fromBlock(Block.COBBLESTONE), ItemThumbnailAlignment.Block),
    BEDROCK    (17, ItemModel.fromBlock(Block.BEDROCK), ItemThumbnailAlignment.Block),
    SAND       (18, ItemModel.fromBlock(Block.SAND), ItemThumbnailAlignment.Block),
    GRAVEL     (19, ItemModel.fromBlock(Block.GRAVEL), ItemThumbnailAlignment.Block),
    OAK_LOG    (20, ItemModel.fromBlock(Block.OAK_LOG), ItemThumbnailAlignment.Block),
    IRON_BLOCK (21, ItemModel.fromBlock(Block.IRON_BLOCK), ItemThumbnailAlignment.Block),
    GOLD_BLOCK (22, ItemModel.fromBlock(Block.GOLD_BLOCK), ItemThumbnailAlignment.Block),

    GOLD_ORE  (32, ItemModel.fromBlock(Block.GOLD_ORE), ItemThumbnailAlignment.Block),
    IRON_ORE  (33, ItemModel.fromBlock(Block.IRON_ORE), ItemThumbnailAlignment.Block),
    COAL_ORE  (34, ItemModel.fromBlock(Block.COAL_ORE), ItemThumbnailAlignment.Block),
    BOOKSHELF (35, ItemModel.fromBlock(Block.BOOKSHELF), ItemThumbnailAlignment.Block),
    MOSSY_COBBLESTONE(36, ItemModel.fromBlock(Block.MOSSY_COBBLESTONE), ItemThumbnailAlignment.Block),
    OBSIDIAN  (37, ItemModel.fromBlock(Block.OBSIDIAN), ItemThumbnailAlignment.Block),

    SPONGE      (48, ItemModel.fromBlock(Block.SPONGE), ItemThumbnailAlignment.Block),
    GLASS       (49, ItemModel.fromBlock(Block.GLASS), ItemThumbnailAlignment.Block),
    DIAMOND_ORE (50, ItemModel.fromBlock(Block.DIAMOND_ORE), ItemThumbnailAlignment.Block),
    REDSTONE_ORE(51, ItemModel.fromBlock(Block.REDSTONE_ORE), ItemThumbnailAlignment.Block),
    OAK_LEAVES(53, ItemModel.fromBlock(Block.OAK_LEAVES), ItemThumbnailAlignment.Block),
    STONE_BRICKS (54, ItemModel.fromBlock(Block.STONE_BRICKS), ItemThumbnailAlignment.Block),

    SNOWY_GRASS(66, ItemModel.fromBlock(Block.SNOWY_GRASS), ItemThumbnailAlignment.Block),

    BIRCH_LOG(117, ItemModel.fromBlock(Block.BIRCH_LOG), ItemThumbnailAlignment.Block),
    BIRCH_LEAVES(118, ItemModel.fromBlock(Block.BIRCH_LEAVES), ItemThumbnailAlignment.Block),

    PINE_LOG(116, ItemModel.fromBlock(Block.PINE_LOG), ItemThumbnailAlignment.Block),
    PINE_LEAVES(187, ItemModel.fromBlock(Block.PINE_LEAVES), ItemThumbnailAlignment.Block),

    JUNGLE_LOG(153, ItemModel.fromBlock(Block.JUNGLE_LOG), ItemThumbnailAlignment.Block),
    JUNGLE_LEAVES(188, ItemModel.fromBlock(Block.JUNGLE_LEAVES), ItemThumbnailAlignment.Block);


    private static final ItemType[] vals = new ItemType[4096];
    static {
        for (var item : ItemType.values()) {
            vals[item.getID()] = item;
        }
    }

    private static final float ITEM_SIZE = 0.20f;

    private int id;
    private Model model;
    private ItemThumbnailAlignment thumbnailAlignment;

    ItemType(int id, Model model, ItemThumbnailAlignment thumbnailAlignment) {
        this.id = id;
        this.model = model;
        this.thumbnailAlignment = thumbnailAlignment;
    }

    public int getID() {
        return id;
    }

    public Model getModel() {
        return model;
    }

    public ItemThumbnailAlignment getThumbnailAlignment() {
        return thumbnailAlignment;
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
        EntityManager.addComponent(item, new ItemComponent(itemID));
        EntityManager.addComponent(item, new PhysicsObjectComponent(new Vector3f(0.05f, 0.05f, 0.05f)));

        return item;
    }

}
