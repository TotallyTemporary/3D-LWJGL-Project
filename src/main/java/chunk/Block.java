package chunk;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import item.ItemType;
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

    INVALID  (1, 1, true, new BlockFace[]{ null, null, null, null, null, null }),
    AIR      (0, 0, false, new BlockFace[]{ null, null, null, null, null, null }),
    GRASS    (2, 4, true, makeFaces(SquareBlockFace.class, new int[] { 0, 3, 3, 3, 3, 2 })),
    STONE    (3, 16, true, cubeFaces(1)),
    DIRT     (4, 4, true, cubeFaces(2)),
    OAK_PLANK(5, 5, true, cubeFaces(4)),
    STONE_SLABS           (6, 6, true, cubeFaces(5)),
    CHISELLED_STONE_BRICKS(7, 7, true, cubeFaces(6)),
    BRICKS     (8, 8, true, cubeFaces(7)),
    TNT        (9, 9, true, makeFaces(SquareBlockFace.class, new int[] { 10, 9, 9, 9, 9, 11 })),
    COBWEB     (11, 1, false, decorFaces(11)),
    ROSE       (12, 12, false, decorFaces(12)),
    DANDELION  (13, 13, false, decorFaces(13)),
    OAK_SAPLING(15, 1, false, decorFaces(15)),

    COBBLESTONE(16, 16, true, cubeFaces(16)),
    BEDROCK    (17, 1, true, cubeFaces(17)),
    SAND       (18, 18, true, cubeFaces(18)),
    GRAVEL     (19, 19, true, cubeFaces(19)),
    OAK_LOG    (20, 20, true, makeFaces(SquareBlockFace.class, new int[] { 21, 20, 20, 20, 20, 21 })),
    IRON_BLOCK (21, 21, true, cubeFaces(21)),
    GOLD_BLOCK (22, 22, true, cubeFaces(22)),

    GOLD_ORE  (32, 32, true, cubeFaces(32)),
    IRON_ORE  (33, 33, true, cubeFaces(33)),
    COAL_ORE  (34, 34, true, cubeFaces(34)),
    BOOKSHELF (35, 35, true, cubeFaces(35)),
    MOSSY_COBBLESTONE(36, 16, true, cubeFaces(36)),
    OBSIDIAN  (37, 37, true, cubeFaces(37)),

    SPONGE      (48, 48, true, cubeFaces(48)),
    GLASS       (49, 1, true, cubeFaces(49)),
    DIAMOND_ORE (50, 50, true, cubeFaces(50)),
    REDSTONE_ORE(51, 51, true, cubeFaces(51)),
    // OAK_LEAVES_HQ(52, cubeFaces(52)), // texture pack png didn't have alpha anyway.
    OAK_LEAVES(53, 53, true, cubeFaces(53)),
    STONE_BRICKS (54, 54, true, cubeFaces(54)),

    CACTUS(69, 4, true, makeFaces(SquareBlockFace.class, new int[] { 69, 70, 70, 70, 70, 71 }));

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

    private boolean hasTransparentFace;

    private Model blockBreakModel = null;

    Block(int id, int item, boolean isSolid, BlockFace[] faces) {
        this.id = (byte) id;
        this.item = item;
        this.faces = faces;
        this.isSolid = isSolid;

        this.hasTransparentFace = true;
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

    // make array of references for lookup
    private static final Block[] vals = new Block[Byte.MAX_VALUE];
    static {
        for (var block : Block.values()) {
            vals[block.getID()] = block;
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
        return vals[id];
    }

    private static BlockFace[] cubeFaces(int indices) {
        return makeFaces(SquareBlockFace.class, new int[] { indices, indices, indices, indices, indices, indices });
    }

    private static BlockFace[] decorFaces(int indices) {
        return makeFaces(DecorBlockFace.class, new int[] { indices, indices, indices, indices, indices, indices });
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
    private static BlockFace[] makeFaces(Class<? extends BlockFace> clazz, int[] indices) {
        try {
            var constructor = clazz.getDeclaredConstructor(Integer.class, Integer.class);
            var faces = new BlockFace[6];
            for (int i = 0; i < 6; i++) {
                faces[i] = constructor.newInstance(indices[i], i);
            }
            return faces;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.err.println(clazz + " is not a blockface class.");
            e.printStackTrace();
            return null;
        }
    }

}
