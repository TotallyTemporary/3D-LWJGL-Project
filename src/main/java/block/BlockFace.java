package block;

/** Represents one of the 6 faces of a block. */
public abstract class BlockFace {
    // bit flags
    public static final int
        NO_FLAG = 0,
        TRANSPARENT = 1,
        ALPHA_BLEND = 2;

    public final int blockID;
    public final int direction;
    public final int flags;

    public BlockFace(Integer blockID, Integer direction, Integer flags) {
        this.blockID = blockID;
        this.direction = direction;
        this.flags = flags;
    }

    public float[] getVertices() { return new float[0]; }
    public float[] getTextureCoords() { return new float[0]; }
    public boolean isTransparent() { return (flags & TRANSPARENT) == TRANSPARENT; }
    public boolean isAlphaBlended() { return (flags & ALPHA_BLEND) == ALPHA_BLEND; }

    protected float[] addBlockIndex(float[] textureCoords) {
        var newTextureCoords = new float[textureCoords.length/2*3];
        for (var i = 0; i < textureCoords.length/2; i++) {
            newTextureCoords[i*3] = textureCoords[i*2];
            newTextureCoords[i*3+1] = textureCoords[i*2+1];
            newTextureCoords[i*3+2] = blockID;
        }
        return newTextureCoords;
    }

}
