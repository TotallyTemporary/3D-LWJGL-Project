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
    GRASS    (2, ItemModel.from3DBlock(Block.GRASS), ItemThumbnailAlignment.Block),
    STONE    (3, ItemModel.from3DBlock(Block.STONE), ItemThumbnailAlignment.Block),
    DIRT     (4, ItemModel.from3DBlock(Block.DIRT), ItemThumbnailAlignment.Block),
    OAK_PLANK(5, ItemModel.from3DBlock(Block.OAK_PLANK), ItemThumbnailAlignment.Block),
    STONE_SLABS           (6, ItemModel.from3DBlock(Block.STONE_SLABS), ItemThumbnailAlignment.Block),
    CHISELLED_STONE_BRICKS(7, ItemModel.from3DBlock(Block.CHISELLED_STONE_BRICKS), ItemThumbnailAlignment.Block),
    BRICKS     (8, ItemModel.from3DBlock(Block.BRICKS), ItemThumbnailAlignment.Block),
    TNT        (9, ItemModel.from3DBlock(Block.TNT), ItemThumbnailAlignment.Block),
    COBWEB     (11, ItemModel.from3DBlock(Block.COBWEB), ItemThumbnailAlignment.Block),
    ROSE       (12, ItemModel.from2DBlock(Block.ROSE), ItemThumbnailAlignment.Item),
    DANDELION  (13, ItemModel.from2DBlock(Block.DANDELION), ItemThumbnailAlignment.Item),
    OAK_SAPLING(15, ItemModel.from3DBlock(Block.OAK_SAPLING), ItemThumbnailAlignment.Item),

    COBBLESTONE(16, ItemModel.from3DBlock(Block.COBBLESTONE), ItemThumbnailAlignment.Block),
    BEDROCK    (17, ItemModel.from3DBlock(Block.BEDROCK), ItemThumbnailAlignment.Block),
    SAND       (18, ItemModel.from3DBlock(Block.SAND), ItemThumbnailAlignment.Block),
    GRAVEL     (19, ItemModel.from3DBlock(Block.GRAVEL), ItemThumbnailAlignment.Block),
    OAK_LOG    (20, ItemModel.from3DBlock(Block.OAK_LOG), ItemThumbnailAlignment.Block),
    IRON_BLOCK (21, ItemModel.from3DBlock(Block.IRON_BLOCK), ItemThumbnailAlignment.Block),
    GOLD_BLOCK (22, ItemModel.from3DBlock(Block.GOLD_BLOCK), ItemThumbnailAlignment.Block),

    WHEAT(88, ItemModel.from2DItem(25), ItemThumbnailAlignment.Item),
    CARROT(200, ItemModel.from2DItem(120), ItemThumbnailAlignment.Item),
    POTATO(217, ItemModel.from2DItem(119), ItemThumbnailAlignment.Item),

    GOLD_ORE  (32, ItemModel.from3DBlock(Block.GOLD_ORE), ItemThumbnailAlignment.Block),
    IRON_ORE  (33, ItemModel.from3DBlock(Block.IRON_ORE), ItemThumbnailAlignment.Block),
    COAL_ORE  (34, ItemModel.from3DBlock(Block.COAL_ORE), ItemThumbnailAlignment.Block),
    BOOKSHELF (35, ItemModel.from3DBlock(Block.BOOKSHELF), ItemThumbnailAlignment.Block),
    MOSSY_COBBLESTONE(36, ItemModel.from3DBlock(Block.MOSSY_COBBLESTONE), ItemThumbnailAlignment.Block),
    OBSIDIAN  (37, ItemModel.from3DBlock(Block.OBSIDIAN), ItemThumbnailAlignment.Block),

    SPONGE      (48, ItemModel.from3DBlock(Block.SPONGE), ItemThumbnailAlignment.Block),
    GLASS       (49, ItemModel.from3DBlock(Block.GLASS), ItemThumbnailAlignment.Block),
    DIAMOND_ORE (50, ItemModel.from3DBlock(Block.DIAMOND_ORE), ItemThumbnailAlignment.Block),
    REDSTONE_ORE(51, ItemModel.from3DBlock(Block.REDSTONE_ORE), ItemThumbnailAlignment.Block),
    OAK_LEAVES(53, ItemModel.from3DBlock(Block.OAK_LEAVES), ItemThumbnailAlignment.Block),
    STONE_BRICKS (54, ItemModel.from3DBlock(Block.STONE_BRICKS), ItemThumbnailAlignment.Block),

    SNOWY_GRASS(66, ItemModel.from3DBlock(Block.SNOWY_GRASS), ItemThumbnailAlignment.Block),

    BIRCH_LOG(117, ItemModel.from3DBlock(Block.BIRCH_LOG), ItemThumbnailAlignment.Block),
    BIRCH_LEAVES(118, ItemModel.from3DBlock(Block.BIRCH_LEAVES), ItemThumbnailAlignment.Block),

    PINE_LOG(116, ItemModel.from3DBlock(Block.PINE_LOG), ItemThumbnailAlignment.Block),
    PINE_LEAVES(187, ItemModel.from3DBlock(Block.PINE_LEAVES), ItemThumbnailAlignment.Block),

    JUNGLE_LOG(153, ItemModel.from3DBlock(Block.JUNGLE_LOG), ItemThumbnailAlignment.Block),
    JUNGLE_LEAVES(188, ItemModel.from3DBlock(Block.JUNGLE_LEAVES), ItemThumbnailAlignment.Block);


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
