package block;

import org.joml.Vector3i;

/** The 6 cardinal directions.
 * @see DiagonalDirection for the 26 directions including diagonals. */
public class CardinalDirection {

    public static final int COUNT = 6;

    public static final int
            UP = 0,
            LEFT = 1,
            FRONT = 2,
            BACK = 3,
            RIGHT = 4,
            DOWN = 5;

    public static final Vector3i[] offsets = {
        new Vector3i(0,  +1,   0),
        new Vector3i(-1,  0,   0),
        new Vector3i(0,   0,  -1),
        new Vector3i(0,   0,  +1),
        new Vector3i(+1,  0,   0),
        new Vector3i(0,  -1,   0),
    };

    // TODO NOTE the tangents and bitangents are only used for world generation. They were hand-chosen until it looked fine.
    // please don't read further into these values.

    public static final Vector3i[] tangents = {
        new Vector3i(0,   0,  +1),
        null,
        null,
        new Vector3i(+1,  0,   0),
        new Vector3i(0,   0,   +1),
        null,
    };

    public static final Vector3i[] biTangents = {
        new Vector3i(+1,  0,   0),
        null,
        null,
        new Vector3i(0,  +1,   0),
        new Vector3i(0,  +1,   0),
        null,
    };

    public static int opposite(int dir) {
        return 5-dir;
    }
}
