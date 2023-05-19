package main;

/**
 * This global object keeps track of the time passed between frames.
 * */
public class Timer {

    private static long lastTime = System.nanoTime();
    private static long delta = 0;

    private static int lastFps = 0;
    private static int fpsCounter = 0;
    private static long fpsDelta = 0;

    /** Updates the state of this Timer, call this once and only once, per frame. */
    public static void tick() {
        var now = System.nanoTime();
        delta = now - lastTime;
        lastTime = now;

        fpsCounter += 1;
        fpsDelta += delta;

        if (fpsDelta >= 1_000_000_000) {
            fpsDelta = 0;
            lastFps = fpsCounter;
            fpsCounter = 0;
        }
    }

    /** Gets the time between frames, in milliseconds.
     * internally gets the time passed between last tick() and the tick() before that, in milliseconds. */
    public static float getFrametimeMillis() {
        return delta / 1_000_000f;
    }

    /** Gets the time between frames in seconds. */
    public static float getFrametimeSeconds() {
        return getFrametimeMillis() / 1000f;
    }

    public static int getFps() {
        return lastFps;
    }

}
