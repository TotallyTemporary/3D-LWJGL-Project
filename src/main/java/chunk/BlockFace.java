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
