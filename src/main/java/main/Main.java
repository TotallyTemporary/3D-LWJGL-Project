package main;

import entity.Entity;
import entity.EntityManager;
import entity.ModelComponent;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL30;
import render.Camera;
import render.Model;
import render.Renderer;
import shader.Shader;

public class Main {

    public static void main(String[] args) {
        var display = new Display("A display.", 800, 600, -1, false, Display.DisplayMode.WINDOWED);
        GLFWErrorCallback.createPrint(System.err).set();
        GL30.glClearColor(0.2f, 0.3f, 0.4f, 0f);

        var shader = new Shader(
                "src/main/resources/shaders/vertex_shader.glsl",
                "src/main/resources/shaders/fragment_shader.glsl"
        )
                .addUniform("transformationMatrix")
                .addUniform("projectionMatrix")
                .addUniform("viewMatrix");

        var entity = new Entity();
        EntityManager.addComponent(entity, new ModelComponent(
            new Model()
                .addPosition3D(TestCube.vertices)
                .addIndices(TestCube.indices)
                .setShader(shader)
        ));

        EntityManager.addComponent(entity, new TransformationComponent(
                new Vector3f(0, 0, -10f),
                new Vector3f(0, 0, 0),
                1f
        ));

        var camera = new Camera(
                (float) Math.toRadians(60f),
                (float) display.getWidth() / display.getHeight(),
                0.5f,
                1000f
        );

        var renderer = new Renderer();
        int count = 0;
        while (!GLFW.glfwWindowShouldClose(display.getWindow())) {
            // update
            EntityManager.start();
            count++;
            EntityManager.getComponent(entity, TransformationComponent.class)
                            .setRotation(new Vector3f(count / 1000f, count / 1000f, count / 1000f));
            EntityManager.stop();

            // render
            renderer.render(camera);

            // glfw shit
            GLFW.glfwSwapBuffers(display.getWindow());
            GLFW.glfwPollEvents();

            Timer.fpsTimerUpdate();
        }

        display.destroy();
        GLFW.glfwTerminate();
    }

}
