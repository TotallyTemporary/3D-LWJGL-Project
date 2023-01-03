package main;

import chunk.*;
import entity.*;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL30;
import render.*;
import shader.Shader;

import java.util.Random;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        var displaySettings = new Display.DisplaySettings(
                "A display",   // title
                800, 600,    // resolution
                -1,          // monitor? (-1 gets primary)
                false,       // vsync
                Display.DisplayMode.WINDOWED
        );
        var display = new Display(displaySettings);
        GLFWErrorCallback.createPrint(System.err).set();
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
                new Vector3f(0, 0, 0),
                new Vector3f(0, 0, 0),
                1f
        ));

        var renderer = new Renderer();
        Thread.sleep(5000);
        while (!GLFW.glfwWindowShouldClose(display.getWindow())) {
            // update
            Timer.tick();
            ChunkLoader.update(new Vector3f(0, 0, 0));

            // generate and load chunks
            TerrainGenerator.loadChunks();
            TerrainModelGenerator.loadChunks();
            TerrainModelLoader.loadChunks(shader, blocksTexture);

            EntityManager.start();
            EntityManager.stop();

            // render
            renderer.render(camera);


            // glfw shit
            GLFW.glfwSwapBuffers(display.getWindow());
            GLFW.glfwPollEvents();
        }

        display.destroy();
        GLFW.glfwTerminate();
    }

}
