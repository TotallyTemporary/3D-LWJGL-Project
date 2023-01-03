package chunk;

public enum Direction {
    UP, LEFT, FRONT, BACK, RIGHT, DOWN;

    public static final Direction[] vals = Direction.values();

    public Direction opposite() {
        return vals[opposite(this.ordinal())];
    }

    public static int opposite(int index) {
        return 5-index;
    }
}
