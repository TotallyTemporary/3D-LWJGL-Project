package chunk;

import java.lang.reflect.InvocationTargetException;

public enum Block {
    AIR(0, null),
    STONE(1, makeFaces(DefaultBlockFace.class, new int[] { 1, 1, 1, 1, 1, 1 }));


    private byte id;
    private BlockFace[] faces;

    Block(int id, BlockFace[] faces) {
        this.id = (byte) id;
        this.faces = faces;
    }

    public BlockFace getFace(int index) {
        return faces[index];
    }

    public byte getID() {
        return id;
    }

    private static final Block[] vals = Block.values();
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
            var constructor = clazz.getDeclaredConstructor(Integer.class, BlockFace.Direction.class);
            var faces = new BlockFace[6];
            for (int i = 0; i < 6; i++) {
                faces[i] = constructor.newInstance(indices[i], BlockFace.Direction.values()[i]);
            }
            return faces;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.err.println(clazz + " is not a blockface class.");
            e.printStackTrace();
            return null;
        }
    }

}
