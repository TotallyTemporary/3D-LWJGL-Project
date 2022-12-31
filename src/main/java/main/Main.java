package main;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

public class Main {

    public static void main(String[] args) {
        var display = new Display("A display.", 800, 600, -1, false, Display.DisplayMode.WINDOWED);
        GL.createCapabilities();
        GLFWErrorCallback.createPrint(System.err).set();
        GL30.glClearColor(1f, 0f, 0f, 0f);

        while (!GLFW.glfwWindowShouldClose(display.getWindow())) {
            GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

            GLFW.glfwSwapBuffers(display.getWindow());
            GLFW.glfwPollEvents();
            Timer.fpsTimerUpdate();
        }

        display.destroy();
        GLFW.glfwTerminate();
    }

}
