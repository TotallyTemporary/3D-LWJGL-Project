package block;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import item.ItemType;
import item.ToolType;
import render.Model;
import render.Texture;
import shader.Shader;

import java.lang.reflect.InvocationTargetException;

/** Represents data about a particular block.
 * In chunks, blocks are represented by a single byte, which is used to look up the block data from the Block enum.
 * */
public enum Block {
    // block faces: UP, LEFT, FRONT, BACK, RIGHT, DOWN;
    // null block face is invisible.
    // use the `makeFaces` -method to create multiple faces of the same type, with different indices.
    // the `cubeFaces` and `decorFaces` -methods call the `makeFaces` -method with the same index for all faces.

    INVALID  (1, 1, true, new BlockFace[]{ null, null, null, null, null, null }, -1),
    AIR      (0, 0, false, new BlockFace[]{ null, null, null, null, null, null }, -1),
    GRASS    (2, 4, true, makeFaces(SquareBlockFace.class, new int[] { 0, 3, 3, 3, 3, 2 }, BlockFace.NO_FLAG), 0.5f, ToolType.SHOVEL),
    STONE    (3, 16, true, cubeFaces(1), 5f, ToolType.PICKAXE),
    DIRT     (4, 4, true, cubeFaces(2), 0.5f, ToolType.SHOVEL),
    OAK_PLANK(5, 5, true, cubeFaces(4), 2f, ToolType.AXE),
    STONE_SLABS           (6, 6, true, cubeFaces(5), 5f, ToolType.PICKAXE),
    CHISELLED_STONE_BRICKS(7, 7, true, cubeFaces(6), 5f, ToolType.PICKAXE),
    BRICKS     (8, 8, true, cubeFaces(7), 5f, ToolType.PICKAXE),
    TNT        (9, 9, true, makeFaces(SquareBlockFace.class, new int[] { 10, 9, 9, 9, 9, 11 }, BlockFace.NO_FLAG), 0f),
    COBWEB     (11, 1, false, decorFaces(11), 5f),
    ROSE       (12, 12, false, decorFaces(12), 0),
    DANDELION  (13, 13, false, decorFaces(13), 0),
    OAK_SAPLING(15, 1, false, decorFaces(15), 0),

    COBBLESTONE(16, 16, true, cubeFaces(16), 5f, ToolType.PICKAXE),
    BEDROCK    (17, 1, true, cubeFaces(17), 9999f),
    SAND       (18, 18, true, cubeFaces(18), 0.75f, ToolType.SHOVEL),
    GRAVEL     (19, 19, true, cubeFaces(19), 0.75f, ToolType.SHOVEL),
    OAK_LOG    (20, 20, true, makeFaces(SquareBlockFace.class, new int[] { 21, 20, 20, 20, 20, 21 }, BlockFace.NO_FLAG), 2f, ToolType.AXE),
    IRON_BLOCK (21, 21, true, cubeFaces(21), 7f, ToolType.PICKAXE),
    GOLD_BLOCK (22, 22, true, cubeFaces(22), 6f, ToolType.PICKAXE),

    GOLD_ORE  (32, 32, true, cubeFaces(32), 7f, ToolType.PICKAXE),
    IRON_ORE  (33, 33, true, cubeFaces(33), 7f, ToolType.PICKAXE),
    COAL_ORE  (34, 34, true, cubeFaces(34), 4f, ToolType.PICKAXE),
    BOOKSHELF (35, 35, true, cubeFaces(35), 2f, ToolType.AXE),
    MOSSY_COBBLESTONE(36, 16, true, cubeFaces(36), 4f, ToolType.PICKAXE),
    OBSIDIAN  (37, 37, true, cubeFaces(37), 15f, ToolType.PICKAXE),

    TALL_GRASS(39, 1, false, decorFaces(39), 0),
    SHRUB(55, 1, false, decorFaces(55), 0),
    FERN(56, 1, false, decorFaces(56), 0),

    SPONGE      (48, 48, true, cubeFaces(48), 0.5f),
    GLASS       (49, 1, true, cubeFaces(49, BlockFace.TRANSPARENT), 0.5f),
    DIAMOND_ORE (50, 50, true, cubeFaces(50), 4f, ToolType.PICKAXE),
    REDSTONE_ORE(51, 51, true, cubeFaces(51), 4f, ToolType.PICKAXE),
    // OAK_LEAVES_HQ(52, cubeFaces(52)), // texture pack png didn't have alpha anyway.
    OAK_LEAVES(53, 53, true, cubeFaces(53), 0.5f),
    STONE_BRICKS (54, 54, true, cubeFaces(54), 5f, ToolType.PICKAXE),

    SNOWY_GRASS(66, 4, true, makeFaces(SquareBlockFace.class, new int[] { 66, 68, 68, 68, 68, 2 }, BlockFace.NO_FLAG), 0.5f, ToolType.SHOVEL),

    CACTUS(69, 4, true, makeFaces(SquareBlockFace.class, new int[] { 69, 70, 70, 70, 70, 71 }, BlockFace.NO_FLAG), 0.3f, ToolType.AXE),

    WHEAT(88, 1, false, decorFaces(88), 0),
    WHEAT_1(89, 1, false, decorFaces(89), 0),
    WHEAT_2(90, 1, false, decorFaces(90), 0),
    WHEAT_3(91, 1, false, decorFaces(91), 0),
    WHEAT_4(92, 1, false, decorFaces(92), 0),
    WHEAT_5(93, 1, false, decorFaces(93), 0),
    WHEAT_6(94, 1, false, decorFaces(94), 0),
    WHEAT_7(95, 88, false, decorFaces(95), 0),

    BIRCH_LOG(117, 117, true, makeFaces(SquareBlockFace.class, new int[] { 21, 117, 117, 117, 117, 21 }, BlockFace.NO_FLAG), 2f, ToolType.AXE),
    BIRCH_LEAVES(118, 118, true, cubeFaces(53), 0.5f),

    PINE_LOG(116, 116, true, makeFaces(SquareBlockFace.class, new int[] { 21, 116, 116, 116, 116, 21 }, BlockFace.NO_FLAG), 2f, ToolType.AXE),
    PINE_LEAVES(187, 187, true, cubeFaces(53), 0.5f),

    JUNGLE_LOG(153, 153, true, makeFaces(SquareBlockFace.class, new int[] { 21, 153, 153, 153, 153, 21 }, BlockFace.NO_FLAG), 2f, ToolType.AXE),
    JUNGLE_LEAVES(188, 188, true, cubeFaces(53), 0.5f),

    CARROT(200, 1, false, decorFaces(200), 0),
    CARROT_1(201, 1, false, decorFaces(201), 0),
    CARROT_2(202, 1, false, decorFaces(202), 0),
    CARROT_3(203, 200, false, decorFaces(203), 0),

    POTATO(217, 1, false, decorFaces(200), 0),
    POTATO_1(218, 1, false, decorFaces(201), 0),
    POTATO_2(219, 1, false, decorFaces(202), 0),
    POTATO_3(220, 217, false, decorFaces(204), 0),

    WATER(205, 1, false, cubeFaces(205, BlockFace.TRANSPARENT | BlockFace.ALPHA_BLEND), 0);

    // set the `isTransparent` -flag.
    static {
        // for some reason calculating this in the constructor makes the enum null.
        for (var block : Block.values()) {
            block.hasTransparentFace = false;
            for (var face : block.faces) {
                if (face == null || face.isTransparent()) block.hasTransparentFace = true;
            }
        }
    }

    private final byte id;
    private final int item;
    private final BlockFace[] faces;
    private final boolean isSolid;

    private final float breakTimeSeconds;
    private final ToolType correctBreakTool;

    private boolean hasTransparentFace;

    private Model blockBreakModel = null;

    Block(int id, int item, boolean isSolid, BlockFace[] faces, float breakTime, ToolType correctBreakTool) {
        this.id = (byte) id;
        this.item = item;
        this.faces = faces;
        this.isSolid = isSolid;
        this.breakTimeSeconds = breakTime;
        this.correctBreakTool = correctBreakTool;
        this.hasTransparentFace = true;
    }

    Block(int id, int item, boolean isSolid, BlockFace[] faces, float breakTime) {
        this(id, item, isSolid, faces, breakTime, ToolType.NONE);
    }

    public boolean getHasTransparentFace() {
        return this.hasTransparentFace;
    }

    public BlockFace getFace(int index) {
        return faces[index];
    }

    public byte getID() {
        return id;
    }

    public int getItemID() {
        return item;
    }

    public ItemType getItem() {
        return ItemType.getByID(item);
    }

    public boolean isSolid() {
        return isSolid;
    }

    public Model getBreakModel() {
        return blockBreakModel;
    }

    public float getBreakTimeSeconds() {
        return breakTimeSeconds;
    }
    public ToolType getCorrectBreakTool() {
        return correctBreakTool;
    }

    // make array of references for lookup
    private static final Block[] vals = new Block[256];
    static {
        for (var block : Block.values()) {
            vals[block.getID() + 128] = block;
        }
    }

    public static void createBreakModels(Texture blocksTexture, Shader blockBreakShader) {
        for (var block : Block.values()) {
            var vertices = new FloatArrayList();
            var breakTexCoords = new FloatArrayList();

            for (var face : block.faces) {
                if (face == null) continue;

                vertices.addElements(vertices.size(), face.getVertices());
                breakTexCoords.addElements(breakTexCoords.size(), face.getTextureCoords());
            }

            block.blockBreakModel = new Model()
                    .addPosition3D(vertices.elements())
                    .addTextureCoords3D(breakTexCoords.elements())
                    .setTexture(blocksTexture)
                    .setShader(blockBreakShader)
                    .end();
        }
    }

    public static Block getBlock(byte id) {
        return vals[id + 128];
    }

    private static BlockFace[] cubeFaces(int indices) {
        return cubeFaces(indices, BlockFace.NO_FLAG);
    }

    private static BlockFace[] cubeFaces(int indices, int flags) {
        return makeFaces(
            SquareBlockFace.class,
            new int[] { indices, indices, indices, indices, indices, indices },
            flags
        );
    }

    private static BlockFace[] decorFaces(int indices) {
        return decorFaces(indices, BlockFace.TRANSPARENT);
    }

    private static BlockFace[] decorFaces(int indices, int flags) {
        return makeFaces(
            DecorBlockFace.class,
            new int[] { indices, indices, indices, indices, indices, indices },
            flags
        );
    }

    /* Given a block face class and the face indices, returns an array of instantiated block faces.
    * Basically it replaces writing out:
    * new DefaultBlockFace(1, BlockFace.Direction.UP),
    * new DefaultBlockFace(1, BlockFace.Direction.DOWN),
    * ...
    *
    * with
    * makeFaces(DefaultBlockFace.class, new int[]{1, 1, ...});
    * */
    private static BlockFace[] makeFaces(Class<? extends BlockFace> clazz, int[] indices, int[] flags) {
        try {
            var constructor = clazz.getDeclaredConstructor(Integer.class, Integer.class, Integer.class);
            var faces = new BlockFace[6];
            for (int i = 0; i < 6; i++) {
                faces[i] = constructor.newInstance(indices[i], i, flags[i]);
            }
            return faces;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.err.println(clazz + " is not a blockface class.");
            e.printStackTrace();
            return null;
        }
    }

    private static BlockFace[] makeFaces(Class<? extends BlockFace> clazz, int[] indices, int flags) {
        return makeFaces(clazz, indices, new int[] { flags, flags, flags, flags, flags, flags });
    }

}
