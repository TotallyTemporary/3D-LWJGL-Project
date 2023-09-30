package debug;

import java.util.HashMap;

public class DebugTimer {

    private static HashMap<String, Long> startTimes = new HashMap<>();
    private static HashMap<String, Long> durations = new HashMap<>();

    public static void start(String name) {
        startTimes.put(name, System.currentTimeMillis());
    }

    public static void stop(String name) {
        long duration = System.currentTimeMillis() - startTimes.get(name);
        duration += get(name);
        durations.put(name, duration);
    }

    public static long get(String name) {
        return durations.getOrDefault(name, 0L);
    }

    public static void printAll() {
        System.out.println("BEGIN");
        for (String name : durations.keySet()) {
            System.out.println(name + " time: " + get(name) + " ms");
        }
        System.out.println("END");
    }

    public static void clear() {
        startTimes.clear();
        durations.clear();
    }

}
