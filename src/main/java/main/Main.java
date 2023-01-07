package main;

import chunk.*;
import entity.*;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.Configuration;
import render.*;
import shader.Shader;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Configuration.DEBUG_STACK.set(true);
        Configuration.DEBUG.set(true);

        var displaySettings = new Display.DisplaySettings(
                "A display",   // title
                1280, 720,     // resolution
                -1,            // monitor? (-1 gets primary)
                false,         // vsync
                Display.DisplayMode.WINDOWED
        );
        var display = new Display(displaySettings);
        GLFWErrorCallback.createPrint(System.err).set();
        Keyboard.init(display.getWindow());
        GL30.glClearColor(0.2f, 0.3f, 0.4f, 0f);

        var shader = new Shader(
                "src/main/resources/shaders/vertex_shader.glsl",
                "src/main/resources/shaders/fragment_shader.glsl"
        )
                .addUniform("transformationMatrix")
                .addUniform("projectionMatrix")
                .addUniform("viewMatrix");

        var blocksTexture = new ArrayTexture(
                            "src/main/resources/blocks_array.png",
                            "arrayTexture", 16, 16);

        var camera = new Camera(
                (float) Math.toRadians(60f),
                (float) display.getWidth() / display.getHeight(),
                0.5f,
                1000f
        );
        EntityManager.addComponent(camera, new TransformationComponent(
                new Vector3f(1000f, 70f, 1000f),
                new Vector3f(0, 0, 0),
                1f
        ));
        EntityManager.addComponent(camera, new CameraController());

        var renderer = new Renderer();
        while (!GLFW.glfwWindowShouldClose(display.getWindow())) {
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_K)) {
                GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_LINE);
            } else {
                GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_FILL);
            }

            GL30.glCullFace(GL30.GL_BACK);
            GL30.glEnable(GL30.GL_CULL_FACE);

            // update
            Timer.tick();
            {
                var transform = EntityManager.getComponent(camera, TransformationComponent.class);
                ChunkLoader.startUpdate(transform.getPosition());
            }

            TerrainModelLoader.loadChunks(shader, blocksTexture); // has to be called before transformation components are updated
            EntityManager.start();
            EntityManager.stop();

            // end update

            // render
            renderer.render(camera);
            GLFW.glfwSetWindowTitle(display.getWindow(), (int) Timer.getFrametimeMillis() + " ms");

            // glfw stuff
            GLFW.glfwSwapBuffers(display.getWindow());
            GLFW.glfwPollEvents();
        }
        TerrainGenerator.stop();
        StructureGenerator.stop();
        TerrainModelGenerator.stop();

        Model.destroy();
        blocksTexture.destroy();
        shader.destroy();

        Keyboard.destroy(display.getWindow());
        GL.setCapabilities(null);
        display.destroy();
        GLFW.glfwSetErrorCallback(null).free();
        GLFW.glfwTerminate();
    }

}
