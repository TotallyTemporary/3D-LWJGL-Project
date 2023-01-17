package main;

import chunk.*;
import entity.*;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.Configuration;
import player.Keyboard;
import player.Mouse;
import player.PlayerBlockController;
import player.PlayerMovementController;
import render.*;
import shader.Shader;

public class Main {

    public static void main(String[] args) {
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

        GL30.glClearColor(0.2f, 0.3f, 0.4f, 0f);

        var terrainShader = new Shader(
                "src/main/resources/shaders/terrain_vertex.glsl",
                "src/main/resources/shaders/terrain_fragment.glsl"
        )
                .addUniform("transformationMatrix")
                .addUniform("projectionMatrix")
                .addUniform("viewMatrix");

        var terrainTexture = new ArrayTexture(
                            "src/main/resources/blocks_array.png",
                            "arrayTexture", 16, 16);

        TerrainModelLoader.setChunkTexture(terrainTexture);
        TerrainModelLoader.setShader(terrainShader);

        var uiTexture = new ArrayTexture(
                "src/main/resources/ui_array.png",
                "arrayTexture", 16, 16);

        var uiShader = new Shader(
                "src/main/resources/shaders/ui_vertex.glsl",
                "src/main/resources/shaders/ui_fragment.glsl"
        )
                .addUniform("transformationMatrix");

        var crosshair = new Entity();
        var as = (float) display.getWidth() / display.getHeight();
        EntityManager.addComponent(crosshair, new UIModelComponent(
                new Model()
                    .addPosition3D(new float[] {
                        -1f / as, -1f, -1f,
                         1f / as, -1f, -1f,
                         1f / as,  1f, -1f,
                         1f / as,  1f, -1f,
                        -1f / as,  1f, -1f,
                        -1f / as, -1f, -1f
                    })
                    .addTextureCoords3D(
                            new float[]{
                        0f, 1f, 15f,
                        1f, 1f, 15f,
                        1f, 0f, 15f,
                        1f, 0f, 15f,
                        0f, 0f, 15f,
                        0f, 1f, 15f,
                    })
                    .setTexture(uiTexture)
                    .setShader(uiShader)
                    .end()
        ));
        EntityManager.addComponent(crosshair, new TransformationComponent(new Vector3f(), new Vector3f(), 0.05f));

        var playerStartPosition = new Vector3f(1000f, 120f, 1000f);

        var camera = new Camera(
                (float) Math.toRadians(60f),
                (float) display.getWidth() / display.getHeight(),
                0.1f,
                1000f
        );
        EntityManager.addComponent(camera, new TransformationComponent(
                playerStartPosition,
                new Vector3f(0, 0, 0),
                1f
        ));
        EntityManager.addComponent(camera, new PlayerMovementController());
        EntityManager.addComponent(camera, new PlayerBlockController());

        Keyboard.init(display.getWindow());
        Mouse.init(display.getWindow(),
                () -> {
                    var blockController = EntityManager.getComponent(camera, PlayerBlockController.class);
                    Vector3i pos;
                    if ((pos = blockController.getHitBlock()) != null) {
                        ChunkLoader.setBlockAt(pos, Block.AIR.getID());
                        ChunkLoader.updateSpoiled();
                    }
                },
                () -> {
                    var blockController = EntityManager.getComponent(camera, PlayerBlockController.class);
                    Vector3i pos;
                    if ((pos = blockController.getBeforeHitBlock()) != null) {
                        ChunkLoader.setBlockAt(pos, Block.COBBLESTONE.getID());
                        ChunkLoader.updateSpoiled();
                    }
                });

        var renderer = new Renderer();

        /*while (ChunkLoader.update(playerStartPosition) > 0 || ChunkLoader.getQueueSize() > 0) {
            TerrainModelLoader.loadChunks(999);
        }*/

        while (!GLFW.glfwWindowShouldClose(display.getWindow())) {
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_K)) {
                GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_LINE);
            } else {
                GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_FILL);
            }

            // update
            Timer.tick();
            {
                var transform = EntityManager.getComponent(camera, TransformationComponent.class);
                ChunkLoader.update(transform.getPosition());
            }

            EntityManager.start();

            // these control chunks so call them before transformation components are updated to prevent flicker.
            TerrainModelLoader.loadChunks();
            EntityManager.updateComponents(PlayerMovementController.class);
            EntityManager.updateComponents(PlayerBlockController.class);

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

        Model.destroyAll();
        terrainTexture.destroy();
        terrainShader.destroy();

        Keyboard.destroy(display.getWindow());
        GL.setCapabilities(null);
        display.destroy();
        GLFW.glfwSetErrorCallback(null).free();
        GLFW.glfwTerminate();
    }

}
