package chunk;

import org.joml.Vector3i;

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
            new Vector3i(0,   0,   1),
            new Vector3i(+1,  0,   0),
            new Vector3i(0,  -1,   0),
    };

    public static int opposite(int dir) {
        return 5-dir;
    }
}
