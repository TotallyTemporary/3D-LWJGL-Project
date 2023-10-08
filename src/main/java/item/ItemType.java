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

    INVALID  (1, ItemModel.noModel(), ItemThumbnailAlignment.Block, 1),
    AIR      (0, ItemModel.noModel(), ItemThumbnailAlignment.Block, 1),
    GRASS    (2, ItemModel.from3DBlock(Block.GRASS), ItemThumbnailAlignment.Block, 2),
    STONE    (3, ItemModel.from3DBlock(Block.STONE), ItemThumbnailAlignment.Block, 3),
    DIRT     (4, ItemModel.from3DBlock(Block.DIRT), ItemThumbnailAlignment.Block, 4),
    OAK_PLANK(5, ItemModel.from3DBlock(Block.OAK_PLANK), ItemThumbnailAlignment.Block, 5),
    STONE_SLABS           (6, ItemModel.from3DBlock(Block.STONE_SLABS), ItemThumbnailAlignment.Block, 6),
    CHISELLED_STONE_BRICKS(7, ItemModel.from3DBlock(Block.CHISELLED_STONE_BRICKS), ItemThumbnailAlignment.Block, 7),
    BRICKS     (8, ItemModel.from3DBlock(Block.BRICKS), ItemThumbnailAlignment.Block, 8),
    TNT        (9, ItemModel.from3DBlock(Block.TNT), ItemThumbnailAlignment.Block, 9),
    COBWEB     (11, ItemModel.from3DBlock(Block.COBWEB), ItemThumbnailAlignment.Block, 11),
    ROSE       (12, ItemModel.from2DBlock(Block.ROSE), ItemThumbnailAlignment.Item, 12),
    DANDELION  (13, ItemModel.from2DBlock(Block.DANDELION), ItemThumbnailAlignment.Item, 13),
    OAK_SAPLING(15, ItemModel.from3DBlock(Block.OAK_SAPLING), ItemThumbnailAlignment.Item, 15),

    COBBLESTONE(16, ItemModel.from3DBlock(Block.COBBLESTONE), ItemThumbnailAlignment.Block, 16),
    BEDROCK    (17, ItemModel.from3DBlock(Block.BEDROCK), ItemThumbnailAlignment.Block, 17),
    SAND       (18, ItemModel.from3DBlock(Block.SAND), ItemThumbnailAlignment.Block, 18),
    GRAVEL     (19, ItemModel.from3DBlock(Block.GRAVEL), ItemThumbnailAlignment.Block, 19),
    OAK_LOG    (20, ItemModel.from3DBlock(Block.OAK_LOG), ItemThumbnailAlignment.Block, 20),
    IRON_BLOCK (21, ItemModel.from3DBlock(Block.IRON_BLOCK), ItemThumbnailAlignment.Block, 21),
    GOLD_BLOCK (22, ItemModel.from3DBlock(Block.GOLD_BLOCK), ItemThumbnailAlignment.Block, 22),

    WHEAT(88, ItemModel.from2DItem(25), ItemThumbnailAlignment.Item, 1),
    CARROT(200, ItemModel.from2DItem(120), ItemThumbnailAlignment.Item, 1),
    POTATO(217, ItemModel.from2DItem(119), ItemThumbnailAlignment.Item, 1),

    GOLD_ORE  (32, ItemModel.from3DBlock(Block.GOLD_ORE), ItemThumbnailAlignment.Block, 32),
    IRON_ORE  (33, ItemModel.from3DBlock(Block.IRON_ORE), ItemThumbnailAlignment.Block, 33),
    COAL_ORE  (34, ItemModel.from3DBlock(Block.COAL_ORE), ItemThumbnailAlignment.Block, 34),
    BOOKSHELF (35, ItemModel.from3DBlock(Block.BOOKSHELF), ItemThumbnailAlignment.Block, 35),
    MOSSY_COBBLESTONE(36, ItemModel.from3DBlock(Block.MOSSY_COBBLESTONE), ItemThumbnailAlignment.Block, 36),
    OBSIDIAN  (37, ItemModel.from3DBlock(Block.OBSIDIAN), ItemThumbnailAlignment.Block, 37),

    SPONGE      (48, ItemModel.from3DBlock(Block.SPONGE), ItemThumbnailAlignment.Block, 48),
    GLASS       (49, ItemModel.from3DBlock(Block.GLASS), ItemThumbnailAlignment.Block, 49),
    DIAMOND_ORE (50, ItemModel.from3DBlock(Block.DIAMOND_ORE), ItemThumbnailAlignment.Block, 50),
    REDSTONE_ORE(51, ItemModel.from3DBlock(Block.REDSTONE_ORE), ItemThumbnailAlignment.Block, 51),
    OAK_LEAVES(53, ItemModel.from3DBlock(Block.OAK_LEAVES), ItemThumbnailAlignment.Block, 53),
    STONE_BRICKS (54, ItemModel.from3DBlock(Block.STONE_BRICKS), ItemThumbnailAlignment.Block, 54),

    SNOWY_GRASS(66, ItemModel.from3DBlock(Block.SNOWY_GRASS), ItemThumbnailAlignment.Block, 66),

    BIRCH_LOG(117, ItemModel.from3DBlock(Block.BIRCH_LOG), ItemThumbnailAlignment.Block, 117),
    BIRCH_LEAVES(118, ItemModel.from3DBlock(Block.BIRCH_LEAVES), ItemThumbnailAlignment.Block, 118),

    PINE_LOG(116, ItemModel.from3DBlock(Block.PINE_LOG), ItemThumbnailAlignment.Block, 116),
    PINE_LEAVES(187, ItemModel.from3DBlock(Block.PINE_LEAVES), ItemThumbnailAlignment.Block, 187),

    JUNGLE_LOG(153, ItemModel.from3DBlock(Block.JUNGLE_LOG), ItemThumbnailAlignment.Block, 153),
    JUNGLE_LEAVES(188, ItemModel.from3DBlock(Block.JUNGLE_LEAVES), ItemThumbnailAlignment.Block, 188),

    // items
    IRON_SWORD(322, ItemModel.from2DItem(66), ItemThumbnailAlignment.Item, 1, ToolType.SWORD, 2f),
    IRON_SHOVEL(338, ItemModel.from2DItem(82), ItemThumbnailAlignment.Item, 1, ToolType.SHOVEL, 2f),
    IRON_PICKAXE(354, ItemModel.from2DItem(98), ItemThumbnailAlignment.Item, 1, ToolType.PICKAXE, 2f),
    IRON_AXE(370, ItemModel.from2DItem(114), ItemThumbnailAlignment.Item, 1, ToolType.AXE, 2f),
    IRON_HOE(386, ItemModel.from2DItem(130), ItemThumbnailAlignment.Item, 1, ToolType.HOE, 2f);


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

    private int placeBlockId;

    private ToolType toolType;
    private float toolTypeMultiplier;


    ItemType(int id, Model model, ItemThumbnailAlignment thumbnailAlignment, int placeBlockId,
             ToolType toolType, float toolbreakMultiplier) {
        this.id = id;
        this.model = model;
        this.thumbnailAlignment = thumbnailAlignment;
        this.placeBlockId = placeBlockId;
        this.toolType = toolType;
        this.toolTypeMultiplier = toolbreakMultiplier;
    }

    ItemType(int id, Model model, ItemThumbnailAlignment thumbnailAlignment, int placeBlockId) {
        this(id, model, thumbnailAlignment, placeBlockId, ToolType.NONE, 1f);
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

    public Block getPlaceBlock() {
        return Block.getBlock((byte) placeBlockId);
    }

    public ToolType getToolType() {
        return toolType;
    }

    public float getCorrectToolMultiplier() {
        return toolTypeMultiplier;
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
