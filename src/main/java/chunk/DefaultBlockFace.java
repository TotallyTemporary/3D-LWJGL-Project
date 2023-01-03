package chunk;

public class DefaultBlockFace extends BlockFace {

    /* This class represents a cubic block. It has flat rectangles for faces in all directions. */
    public DefaultBlockFace(Integer blockID, Direction direction) {
        super(blockID, direction);
    }

    /* thank you Jack Mott.
    * https://gist.github.com/jackmott/6e06daa504b070da1ce1ec580e4af59b
    * so i didn't have to write all these out manually. */
    private static float[] FRONT_VERTS = {
        0f, 0f, 0f,
        1f, 0f, 0f,
        1f, 1f, 0f,
        1f, 1f, 0f,
        0f, 1f, 0f,
        0f, 0f, 0f
    };

    private static float[] FRONT_TEX = {
        0f, 1f,
        1f, 1f,
        1f, 0f,
        1f, 0f,
        0f, 0f,
        0f, 1f
    };

    private static float[] BACK_VERTS = {
        0f, 0f, 1f,
        1f, 0f, 1f,
        1f, 1f, 1f,
        1f, 1f, 1f,
        0f, 1f, 1f,
        0f, 0f, 1f
    };

    private static float[] BACK_TEX = {
        0f, 1f,
        1f, 1f,
        1f, 0f,
        1f, 0f,
        0f, 0f,
        0f, 1f
    };

    private static float[] LEFT_VERTS = {
        0f, 1f, 1f,
        0f, 1f, 0f,
        0f, 0f, 0f,
        0f, 0f, 0f,
        0f, 0f, 1f,
        0f, 1f, 1f
    };

    private static float[] LEFT_TEX = {
        0f, 0f,
        1f, 0f,
        1f, 1f,
        1f, 1f,
        0f, 1f,
        0f, 0f
    };

    private static float[] RIGHT_VERTS = {
        1f, 1f, 1f,
        1f, 1f, 0f,
        1f, 0f, 0f,
        1f, 0f, 0f,
        1f, 0f, 1f,
        1f, 1f, 1f
    };

    private static float[] RIGHT_TEX = {
        0f, 0f,
        1f, 0f,
        1f, 1f,
        1f, 1f,
        0f, 1f,
        0f, 0f
    };

    private static float[] TOP_VERTS = {
        0f, 1f, 0f,
        1f, 1f, 0f,
        1f, 1f, 1f,
        1f, 1f, 1f,
        0f, 1f, 1f,
        0f, 1f, 0f
    };

    private static float[] TOP_TEX = {
        0f, 1f,
        1f, 1f,
        1f, 0f,
        1f, 0f,
        0f, 0f,
        0f, 1f
    };

    private static float[] BOTTOM_VERTS = {
        0f, 0f, 0f,
        1f, 0f, 0f,
        1f, 0f, 1f,
        1f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 0f
    };

    private static float[] BOTTOM_TEX = {
        0f, 1f,
        1f, 1f,
        1f, 0f,
        1f, 0f,
        0f, 0f,
        0f, 1f
    };

    public boolean isTransparent() { return false; }

    public float[] getVertices() {
        return switch (this.direction) {
            case UP    -> TOP_VERTS;
            case DOWN  -> BOTTOM_VERTS;
            case LEFT  -> LEFT_VERTS;
            case RIGHT -> RIGHT_VERTS;
            case FRONT -> FRONT_VERTS;
            case BACK  -> BACK_VERTS;
        };

    }

    public float[] getTextureCoords() {
        return addBlockIndex(switch (this.direction) {
            case UP    -> TOP_TEX;
            case DOWN  -> BOTTOM_TEX;
            case LEFT  -> LEFT_TEX;
            case RIGHT -> RIGHT_TEX;
            case FRONT -> FRONT_TEX;
            case BACK  -> BACK_TEX;
        });
    }

    private float[] addBlockIndex(float[] textureCoords) {
        var newTextureCoords = new float[textureCoords.length/2*3];
        for (var i = 0; i < textureCoords.length/2; i++) {
            newTextureCoords[i*3] = textureCoords[i*2];
            newTextureCoords[i*3+1] = textureCoords[i*2+1];
            newTextureCoords[i*3+2] = blockID;
        }
        return newTextureCoords;
    }
}
