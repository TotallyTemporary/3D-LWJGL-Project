package main;

public class Timer {

    private static long lastTime = System.currentTimeMillis();
    private static int counter = 0;

    public static void fpsTimerUpdate() {
        counter++;
        long now = System.currentTimeMillis();
        if (now - lastTime > 1000) {
            System.out.println("fps: " + counter);

            counter = 0;
            lastTime = now;
        }
    }

}
