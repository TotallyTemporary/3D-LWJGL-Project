package chunk;

import java.lang.reflect.InvocationTargetException;

public enum Block {
    // UP, LEFT, FRONT, BACK, RIGHT, DOWN;

    INVALID(0, new BlockFace[]{ null, null, null, null, null, null }),
    AIR    (1, new BlockFace[]{ null, null, null, null, null, null }),
    GRASS  (2, makeFaces(DefaultBlockFace.class, new int[] { 0, 3, 3, 3, 3, 2 })),
    STONE  (3, makeFaces(DefaultBlockFace.class, new int[] { 1, 1, 1, 1, 1, 1 })),
    DIRT   (4, makeFaces(DefaultBlockFace.class, new int[] { 2, 2, 2, 2, 2, 2 }));

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
            var constructor = clazz.getDeclaredConstructor(Integer.class, Direction.class);
            var faces = new BlockFace[6];
            for (int i = 0; i < 6; i++) {
                faces[i] = constructor.newInstance(indices[i], Direction.values()[i]);
            }
            return faces;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.err.println(clazz + " is not a blockface class.");
            e.printStackTrace();
            return null;
        }
    }

}
