package main;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.HashMap;

public class Keyboard {

    private static HashMap<Integer, Boolean> keys = new HashMap<>();

    public static void init(long window) {
        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                switch (action) {
                    case GLFW.GLFW_PRESS -> keys.put(key, true);
                    case GLFW.GLFW_RELEASE -> keys.put(key, false);
                    default -> { } // ignore repeats
                }
            }
        });
    }

    public static void destroy(long window) {
        GLFW.glfwSetKeyCallback(window, null).free();
    }

    public static boolean isKeyDown(int keycode) {
        var key = keys.get(keycode);
        if (key == null) return false;
        else return key;
    }


}
