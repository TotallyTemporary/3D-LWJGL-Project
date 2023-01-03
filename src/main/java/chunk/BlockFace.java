package chunk;

public abstract class BlockFace {
    protected int blockID;
    protected int direction;

    public BlockFace(Integer blockID, Integer direction) {
        this.blockID = blockID;
        this.direction = direction;
    }

    public float[] getVertices() { return new float[0]; }
    public float[] getTextureCoords() { return new float[0]; }
    public boolean isTransparent() { return true; }

}
