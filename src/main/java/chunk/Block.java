package chunk;

import java.lang.reflect.InvocationTargetException;

public enum Block {
    // UP, LEFT, FRONT, BACK, RIGHT, DOWN;

    INVALID  (0, new BlockFace[]{ null, null, null, null, null, null }),
    AIR      (1, new BlockFace[]{ null, null, null, null, null, null }),
    GRASS    (2, makeFaces(DefaultBlockFace.class, new int[] { 0, 3, 3, 3, 3, 2 })),
    STONE    (3, cubeFaces(1)),
    DIRT     (4, cubeFaces(2)),
    OAK_PLANK(5, cubeFaces(4)),
    STONE_SLABS           (6, cubeFaces(5)),
    CHISELLED_STONE_BRICKS(7, cubeFaces(6)),
    BRICKS   (8, cubeFaces(7)),
    TNT      (9, makeFaces(DefaultBlockFace.class, new int[] { 10, 9, 9, 9, 9, 11 })),
    // COBWEB   (11, cubeFaces(11)), // TODO replace with another model
    // ROSE     (12, cubeFaces(12)),
    DANDELION(13, decorFaces(13)),
    // OAK_SAPLING(15, cubeFaces(15)),

    COBBLESTONE(16, cubeFaces(16)),
    BEDROCK    (17, cubeFaces(17)),
    SAND       (18, cubeFaces(18)),
    GRAVEL     (19, cubeFaces(19)),
    OAK_LOG    (20, makeFaces(DefaultBlockFace.class, new int[] { 21, 20, 20, 20, 20, 21 })),
    IRON_BLOCK (21, cubeFaces(21)),
    GOLD_BLOCK (22, cubeFaces(22)),

    GOLD_ORE  (32, cubeFaces(32)),
    IRON_ORE  (33, cubeFaces(33)),
    COAL_ORE  (34, cubeFaces(34)),
    BOOKSHELF (35, cubeFaces(35)),
    MOSSY_COBBLESTONE(36, cubeFaces(36)),
    OBSIDIAN  (37, cubeFaces(37)),

    SPONGE      (48, cubeFaces(48)),
    GLASS       (49, cubeFaces(49)),
    DIAMOND_ORE (50, cubeFaces(50)),
    REDSTONE_ORE(51, cubeFaces(51)),
    // OAK_LEAVES_HQ(52, cubeFaces(52)), // texture pack png didn't have alpha anyway.
    OAK_LEAVES(53, cubeFaces(53)),
    STONE_BRICKS (54, cubeFaces(54));





    static {
        // for some reason calculating this in the constructor makes the enum null.
        for (var block : Block.values()) {
            block.hasTransparentFace = false;
            for (var face : block.faces) {
                if (face == null || face.isTransparent()) block.hasTransparentFace = true;
            }
        }
    }

    private byte id;
    private BlockFace[] faces;

    private boolean hasTransparentFace;

    Block(int id, BlockFace[] faces) {
        this.id = (byte) id;
        this.faces = faces;

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

    private static final Block[] vals = new Block[Byte.MAX_VALUE];
    static {
        for (var block : Block.values()) {
            vals[block.getID()] = block;
        }
    }

    public static Block getBlock(byte id) {
        return vals[id];
    }

    private static BlockFace[] cubeFaces(int indices) {
        return makeFaces(DefaultBlockFace.class, new int[] { indices, indices, indices, indices, indices, indices });
    }

    private static BlockFace[] decorFaces(int indices) {
        return makeFaces(DecorBlockFace.class, new int[] { indices, indices, indices, indices, indices, indices });
    }

    /* replaces list of
    * new DefaultBlockFace(1, BlockFace.Direction.UP),
    * new DefaultBlockFace(1, BlockFace.Direction.DOWN),
    * ...
    *
    * with
    * makeFaces(DefaultBlockFace.class, new int[]{1, 1, 1, 1, 1, 1});
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
