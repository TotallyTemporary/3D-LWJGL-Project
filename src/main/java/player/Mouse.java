package player;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;

public class Mouse {

    private static double lastX = 0d, lastY = 0d;
    private static double deltaX = 0d, deltaY = 0d;

    public static void init(long window) {
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
    }

    public static Vector2f getCursorDelta() {
        var vec = new Vector2f((float) deltaX, (float) deltaY);
        deltaX = 0;
        deltaY = 0;
        return vec;
    }
}
