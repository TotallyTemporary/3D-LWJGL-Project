package player;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.nio.DoubleBuffer;

public class Mouse {

    private static double lastX = 0d, lastY = 0d;
    private static double deltaX = 0d, deltaY = 0d;

    private static boolean isFocused = true;

    private static long window = -1;

    public static void init(long _window, Runnable onLeftClick, Runnable onRightClick) {
        window = _window;

        focus();
        GLFW.glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (!isFocused) return;

                deltaX += xpos - lastX;
                deltaY += ypos - lastY;

                lastX = xpos;
                lastY = ypos;
            }
        });

        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (action != GLFW.GLFW_PRESS) return;
                if (button == GLFW.GLFW_MOUSE_BUTTON_1) onLeftClick.run();
                if (button == GLFW.GLFW_MOUSE_BUTTON_2) onRightClick.run();

                if (!isFocused) focus();
            }
        });
    }

   public static void destroy(long window) {
        GLFW.glfwSetCursorPosCallback(window, null).free();
        GLFW.glfwSetMouseButtonCallback(window, null).free();
   }

    public static Vector2f getCursorDelta() {
        var vec = new Vector2f((float) deltaX, (float) deltaY);
        deltaX = 0;
        deltaY = 0;
        return vec;
    }

    public static void focus() {
        isFocused = true;
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        var mousePosNow = getMousePositionNow();
        lastX = mousePosNow.x;
        lastY = mousePosNow.y;
    }

    public static void unfocus() {
        isFocused = false;
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }

    public static void update() {
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_ESCAPE) && isFocused) {
            unfocus();
        }
    }

    private static Vector2d getMousePositionNow() {
        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(window, x, y);
        return new Vector2d(x.get(), y.get());
    }
}
