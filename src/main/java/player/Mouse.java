package player;

import main.Display;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

public class Mouse {

    private static double lastX = 0d, lastY = 0d;
    private static double deltaX = 0d, deltaY = 0d;

    private static boolean isFocused = true;
    private static boolean isLeftClickDown = false;
    private static boolean isRightClickDown = false;

    private static Display display;

    private static List<Runnable> onLeftClick = new ArrayList<>();
    private static List<Runnable> onRightClick = new ArrayList<>();

    public static void subscribeToLeftClick(Runnable left) {
        onLeftClick.add(left);
    }

    public static void subscribeToRightClick(Runnable right) {
        onRightClick.add(right);
    }

    public static boolean isLeftClickDown() {
        return isLeftClickDown;
    }

    public static boolean isRightClickDown() {
        return isRightClickDown;
    }

    public static void init(Display _display) {
        display = _display;

        focus();
        GLFW.glfwSetCursorPosCallback(display.getWindow(), new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
            if (!isFocused) return;

            deltaX += xpos - lastX;
            deltaY += ypos - lastY;

            lastX = xpos;
            lastY = ypos;
            }
        });

        GLFW.glfwSetMouseButtonCallback(display.getWindow(), new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
            if (action == GLFW.GLFW_PRESS) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                    left();
                    isLeftClickDown = true;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
                    right();
                    isRightClickDown = true;
                }
            }

            if (action == GLFW.GLFW_RELEASE) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                    isLeftClickDown = false;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
                    isRightClickDown = false;
                }
            }
            if (!isFocused) focus();
            }
        });
    }

    public static Vector2f getCursorDelta() {
        var vec = new Vector2f((float) deltaX, (float) deltaY);
        deltaX = 0;
        deltaY = 0;
        return vec;
    }

    public static void focus() {
        isFocused = true;
        GLFW.glfwSetInputMode(display.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        var mousePosNow = getMousePositionNow();
        lastX = mousePosNow.x;
        lastY = mousePosNow.y;
    }

    public static void unfocus() {
        isLeftClickDown = false;
        isRightClickDown = false;
        isFocused = false;
        GLFW.glfwSetInputMode(display.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }

    public static void update() {
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_ESCAPE) && isFocused) {
            unfocus();
            if (display.getDisplayMode() == Display.DisplayMode.FULLSCREEN
                    || display.getDisplayMode() == Display.DisplayMode.FULLSCREEN_BORDERLESS) {
                display.close();
            }
        }
    }

    public static void destroy(long window) {
        GLFW.glfwSetCursorPosCallback(window, null).free();
        GLFW.glfwSetMouseButtonCallback(window, null).free();
    }


    private static void left() {
        for (Runnable r : onLeftClick) {
            r.run();
        }
    }

    private static void right() {
        for (Runnable r : onRightClick) {
            r.run();
        }
    }

    private static Vector2d getMousePositionNow() {
        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(display.getWindow(), x, y);
        return new Vector2d(x.get(), y.get());
    }
}
