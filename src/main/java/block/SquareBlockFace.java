package block;

import block.BlockFace;
import block.CardinalDirection;

public class SquareBlockFace extends BlockFace {

    /* This class represents a cubic block. It has flat rectangles for faces in all directions. */
    public SquareBlockFace(Integer blockID, Integer direction) {
        super(blockID, direction);
    }

    /* thank you Jack Mott.
    * https://gist.github.com/jackmott/6e06daa504b070da1ce1ec580e4af59b
    * so I didn't have to write all these out manually. */
    private static final float[] FRONT_VERTS = {
        1f, 1f, 0f,
        1f, 0f, 0f,
        0f, 0f, 0f,
        0f, 0f, 0f,
        0f, 1f, 0f,
        1f, 1f, 0f,
    };

    private static final float[] FRONT_TEX = {
        1f, 0f,
        1f, 1f,
        0f, 1f,
        0f, 1f,
        0f, 0f,
        1f, 0f
    };

    private static final float[] BACK_VERTS = {
        0f, 0f, 1f,
        1f, 0f, 1f,
        1f, 1f, 1f,
        1f, 1f, 1f,
        0f, 1f, 1f,
        0f, 0f, 1f
    };

    private static final float[] BACK_TEX = {
        0f, 1f,
        1f, 1f,
        1f, 0f,
        1f, 0f,
        0f, 0f,
        0f, 1f
    };

    private static final float[] LEFT_VERTS = {
        0f, 1f, 1f,
        0f, 1f, 0f,
        0f, 0f, 0f,
        0f, 0f, 0f,
        0f, 0f, 1f,
        0f, 1f, 1f
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
        1f, 0f, 0f,
        1f, 1f, 0f,
        1f, 1f, 1f,
        1f, 1f, 1f,
        1f, 0f, 1f,
        1f, 0f, 0f,
    };

    private static final float[] RIGHT_TEX = {
        1f, 1f,
        1f, 0f,
        0f, 0f,
        0f, 0f,
        0f, 1f,
        1f, 1f
    };

    private static final float[] TOP_VERTS = {
        1f, 1f, 1f,
        1f, 1f, 0f,
        0f, 1f, 0f,
        0f, 1f, 0f,
        0f, 1f, 1f,
        1f, 1f, 1f
    };

    private static final float[] TOP_TEX = {
        0f, 1f,
        1f, 1f,
        1f, 0f,
        1f, 0f,
        0f, 0f,
        0f, 1f
    };

    private static final float[] BOTTOM_VERTS = {
        0f, 0f, 0f,
        1f, 0f, 0f,
        1f, 0f, 1f,
        1f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 0f
    };

    private static final float[] BOTTOM_TEX = {
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
            case CardinalDirection.UP    -> TOP_VERTS;
            case CardinalDirection.DOWN  -> BOTTOM_VERTS;
            case CardinalDirection.LEFT  -> LEFT_VERTS;
            case CardinalDirection.RIGHT -> RIGHT_VERTS;
            case CardinalDirection.FRONT -> FRONT_VERTS;
            case CardinalDirection.BACK  -> BACK_VERTS;
            default -> throw new IllegalStateException("Unexpected value: " + this.direction);
        };

    }

    public float[] getTextureCoords() {
        return addBlockIndex(switch (this.direction) {
            case CardinalDirection.UP    -> TOP_TEX;
            case CardinalDirection.DOWN  -> BOTTOM_TEX;
            case CardinalDirection.LEFT  -> LEFT_TEX;
            case CardinalDirection.RIGHT -> RIGHT_TEX;
            case CardinalDirection.FRONT -> FRONT_TEX;
            case CardinalDirection.BACK  -> BACK_TEX;
            default -> throw new IllegalStateException("Unexpected value: " + this.direction);
        });
    }
}
