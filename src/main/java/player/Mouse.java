package player;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

public class Mouse {

    private static double lastX = 0d, lastY = 0d;
    private static double deltaX = 0d, deltaY = 0d;

    public static void init(long window, Runnable onLeftClick, Runnable onRightClick) {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        GLFW.glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
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
}
