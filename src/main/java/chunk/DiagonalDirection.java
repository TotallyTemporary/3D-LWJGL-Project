package chunk;

import org.joml.Vector3i;

/** The 26 directions in a grid. Includes diagonal neighbors. */
public class DiagonalDirection {

    public static final int COUNT = 26;

    public static final int
        UP    = 15,
        DOWN  = 10,
        RIGHT = 21,
        LEFT  = 4,
        FRONT = 12,
        BACK  = 13;

    public static final Vector3i[] offsets = {
           new Vector3i(-1, -1, -1),
           new Vector3i(-1, -1,  0),
           new Vector3i(-1, -1,  1),
           new Vector3i(-1,  0, -1),
           new Vector3i(-1,  0,  0),
           new Vector3i(-1,  0,  1),
           new Vector3i(-1,  1, -1),
           new Vector3i(-1,  1,  0),
           new Vector3i(-1,  1,  1),
           new Vector3i( 0, -1, -1),
           new Vector3i( 0, -1,  0),
           new Vector3i( 0, -1,  1),
           new Vector3i( 0,  0, -1),
           new Vector3i( 0,  0,  1),
           new Vector3i( 0,  1, -1),
           new Vector3i( 0,  1,  0),
           new Vector3i( 0,  1,  1),
           new Vector3i( 1, -1, -1),
           new Vector3i( 1, -1,  0),
           new Vector3i( 1, -1,  1),
           new Vector3i( 1,  0, -1),
           new Vector3i( 1,  0,  0),
           new Vector3i( 1,  0,  1),
           new Vector3i( 1,  1, -1),
           new Vector3i( 1,  1,  0),
           new Vector3i( 1,  1,  1),
    };

    /** Given an offset, this method returns the index, or -1 if it wasn't found. */
    public static int indexOf(Vector3i vec) {
        int count = 0;
        for (var offset : offsets) {
            if (vec.equals(offset)) return count;
            count++;
        }
        return -1;
    }

    public static int opposite(int dir) {
        return 25-dir;
    }
}
