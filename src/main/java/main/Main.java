package main;

import chunk.*;
import entity.*;
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
import ui.UIModelComponent;

public class Main {

    public static void main(String[] args) {
        Configuration.DEBUG_STACK.set(true);
        Configuration.DEBUG.set(true);

        // initializing the display also initialized glfw and creates the context.
        var displaySettings = new Display.DisplaySettings(
                "A display",   // title
                1280, 720,     // resolution
                -1,            // monitor? (-1 gets primary)
                false,         // vsync
                Display.DisplayMode.WINDOWED
        );
        var display = new Display(displaySettings);
        GLFWErrorCallback.createPrint(System.err).set();

        int major = GL30.glGetInteger(GL30.GL_MAJOR_VERSION);
        int minor = GL30.glGetInteger(GL30.GL_MINOR_VERSION);
        System.out.println("OpenGL version: " + major + "." + minor);
        System.out.println("Need OpenGL 4.3 to run.");

        if (major < 4 || (major == 4 && minor < 3)) {
            System.out.println("Your OpenGL version can't run this program, sorry!");
            throw new IllegalStateException("Jump to this stacktrace for more information");
            /*
                Most of the code is built to work around OpenGL 3.0, but loading an array texture at
                ArrayTexture.java requires two GL43 calls. There is a way to load it with 3.0, but I
                couldn't get it to work.
             */
        }

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

        UIModelComponent.createUIModels(display);
        var crosshair = new Entity();
        EntityManager.addComponent(crosshair, new UIModelComponent(15));
        EntityManager.addComponent(crosshair, new TransformationComponent(new Vector3f(), new Vector3f(), new Vector3f(0.05f, 0.05f, 0.05f)));

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
                new Vector3f(1f, 1f, 1f)
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

        // render 1 frame before beginning load, later swap this for loading text
        renderer.render(camera);
        GLFW.glfwSwapBuffers(display.getWindow());

        System.out.println("Starting world preload");
        while (ChunkLoader.update(playerStartPosition) > 0 || ChunkLoader.getQueueSize() > 0) {
            TerrainModelLoader.loadChunks(999);
        }
        System.out.println("Chunks loaded");

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
            int verticesRendered = renderer.render(camera);
            GLFW.glfwSetWindowTitle(display.getWindow(),
                    verticesRendered/3 + " triangles @" +
                    (int) Timer.getFrametimeMillis() + " ms " +
                    ChunkLoader.getQueueSize() + " chunks queued");

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

        System.out.println("Thank you for playing wing commander!");
    }

}
