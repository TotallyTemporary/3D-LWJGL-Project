package block;

import block.BlockFace;
import block.CardinalDirection;

public class DecorBlockFace extends BlockFace {

    public DecorBlockFace(Integer blockID, Integer direction) {
        super(blockID, direction);
    }

    // left/right form one cross (no backface culling)

    private static final float[] LEFT_VERTS = {
        1f, 1f, 1f,
        0f, 1f, 0f,
        0f, 0f, 0f,
        0f, 0f, 0f,
        1f, 0f, 1f,
        1f, 1f, 1f
    };

    private static final float[] LEFT_TEX = {
        0f, 0f,
        1f, 0f,
        1f, 1f,
        1f, 1f,
        0f, 1f,
        0f, 0f
    };

    private static final float[] RIGHT_VERTS = {
        0f, 0f, 0f,
        0f, 1f, 0f,
        1f, 1f, 1f,
        1f, 1f, 1f,
        1f, 0f, 1f,
        0f, 0f, 0f
    };

    private static final float[] RIGHT_TEX = {
        0f, 1f,
        0f, 0f,
        1f, 0f,
        1f, 0f,
        1f, 1f,
        0f, 1f
    };

    private static final float[] FRONT_VERTS = {
        0f, 1f, 1f,
        1f, 1f, 0f,
        1f, 0f, 0f,
        1f, 0f, 0f,
        0f, 0f, 1f,
        0f, 1f, 1f
    };

    private static final float[] FRONT_TEX = {
        0f, 0f,
        1f, 0f,
        1f, 1f,
        1f, 1f,
        0f, 1f,
        0f, 0f
    };

    private static final float[] BACK_VERTS = {
        1f, 0f, 0f,
        1f, 1f, 0f,
        0f, 1f, 1f,
        0f, 1f, 1f,
        0f, 0f, 1f,
        1f, 0f, 0f
    };

    private static final float[] BACK_TEX = {
        0f, 1f,
        0f, 0f,
        1f, 0f,
        1f, 0f,
        1f, 1f,
        0f, 1f
    };

    public boolean isTransparent() { return true; }

    public float[] getVertices() {
        return switch (this.direction) {
            case CardinalDirection.LEFT  -> LEFT_VERTS;
            case CardinalDirection.RIGHT -> RIGHT_VERTS;
            case CardinalDirection.FRONT -> FRONT_VERTS;
            case CardinalDirection.BACK  -> BACK_VERTS;
            default -> new float[0];
        };

    }

    public float[] getTextureCoords() {
        return addBlockIndex(switch (this.direction) {
            case CardinalDirection.LEFT  -> LEFT_TEX;
            case CardinalDirection.RIGHT -> RIGHT_TEX;
            case CardinalDirection.FRONT -> FRONT_TEX;
            case CardinalDirection.BACK  -> BACK_TEX;
            default -> new float[0];
        });
    }
}
