package chunk;

public class Direction {

    // the poor man's enum
    // at least this is considered a "constant expression" smh
    public static final int
            UP = 0,
            LEFT = 1,
            FRONT = 2,
            BACK = 3,
            RIGHT = 4,
            DOWN = 5;

    public static int opposite(int dir) {
        return 5-dir;
    }
}
