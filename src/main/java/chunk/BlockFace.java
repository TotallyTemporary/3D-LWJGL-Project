package chunk;

public abstract class BlockFace {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT, FRONT, BACK
    }

    protected int blockID;
    protected Direction direction;

    public BlockFace(Integer blockID, Direction direction) {
        this.blockID = blockID;
        this.direction = direction;
    }

    public float[] getVertices() { return new float[0]; }
    public float[] getTextureCoords() { return new float[0]; }

}
