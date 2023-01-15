package main;

public class Timer {

    private static long lastTime = System.nanoTime();
    private static long delta = 0;

    public static void tick() {
        var now = System.nanoTime();
        delta = now - lastTime;
        lastTime = now;
    }

    public static float getFrametimeMillis() {
        return delta / 1_000_000f;
    }

    public static float getFrametimeSeconds() {
        return getFrametimeMillis() / 1000f;
    }

}
