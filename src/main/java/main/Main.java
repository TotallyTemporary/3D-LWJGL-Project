package main;

import entity.*;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL30;
import render.*;
import shader.Shader;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
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

        var model = new Model()
                    .addPosition3D(TestCube.vertices)
                    .addIndices(TestCube.indices)
                    .addTextureCoords2D(TestCube.textureCoords)
                    .setShader(shader)
                    //.setTexture(new BasicTexture("src/main/resources/blocks_atlas.png", "arrayTexture"))
                    .setTexture(new ArrayTexture(
                            "src/main/resources/blocks_array.png",
                            "arrayTexture", 16, 16)
                    )
                /*.setTexture(new ArrayTexture(
                        "src/main/resources/blocks_atlas.png",
                        "arrayTexture",
                        256, 256))*/
                .end();

        var mainBlock = new Entity();
        EntityManager.addComponent(mainBlock, new ModelComponent(model));
        EntityManager.addComponent(mainBlock, new TransformationComponent(
                new Vector3f(0, 0, -10f),
                new Vector3f(0, 0, 0),
                2f
        ));
        EntityManager.addComponent(mainBlock, new TestSpinComponent());

        /*
        var r = new Random();
        r.setSeed(System.currentTimeMillis());
        for (var i = 0; i < 200; i++) {
            var entity = new Entity();
            EntityManager.addComponent(entity, new ModelComponent(model));
            EntityManager.addComponent(entity, new TransformationComponent(
                    new Vector3f(r.nextInt(100)-50, r.nextInt(100)-50, -r.nextInt(50)),
                    new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat()),
                    1f
            ));
            EntityManager.addComponent(entity, new TestSpinComponent());
        }*/

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
        while (!GLFW.glfwWindowShouldClose(display.getWindow())) {
            // update
            Timer.tick();
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
