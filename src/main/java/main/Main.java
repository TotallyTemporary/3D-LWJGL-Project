package main;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import shader.Shader;

public class Main {

    public static void main(String[] args) {
        var display = new Display("A display.", 800, 600, -1, false, Display.DisplayMode.WINDOWED);
        GL.createCapabilities();
        GLFWErrorCallback.createPrint(System.err).set();
        GL30.glClearColor(1f, 0f, 0f, 0f);

        var shader = new Shader(
                "src/main/resources/shaders/vertex_shader.glsl",
                "src/main/resources/shaders/fragment_shader.glsl"
        );

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
