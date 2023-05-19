package main;

import chunk.*;
import entity.*;
import item.ItemComponent;
import item.ItemType;
import item.ItemModel;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Configuration;
import player.*;
import render.*;
import shader.Shader;
import ui.UIModelComponent;

public class Main {

    public static final boolean DEBUG = false;
    public static Callback debugMessageCallback = null;

    public static void main(String[] args) {
        if (DEBUG) {
            Configuration.DEBUG_STACK.set(true);
            Configuration.DEBUG.set(true);
        }
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
        Capabilities.get();

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

        var itemsShader = new Shader(
            "src/main/resources/shaders/item_vertex.glsl",
            "src/main/resources/shaders/item_fragment.glsl"
        )
        .addUniform("transformationMatrix")
        .addUniform("projectionMatrix")
        .addUniform("viewMatrix");

        var itemsTexture = new ArrayTexture(
            "src/main/resources/items_array.png",
            "arrayTexture", 16, 16);

        ItemModel.init(itemsShader, terrainTexture, itemsTexture);
        DefaultTexture.init();

        UIModelComponent.createUIModels(display);
        var crosshair = new Entity();
        EntityManager.addComponent(crosshair, new UIModelComponent(15));
        EntityManager.addComponent(crosshair, new TransformationComponent(new Vector3f(), new Vector3f(), new Vector3f(0.05f, 0.05f, 0.05f)));

        var playerStartPosition = new Vector3f(1000f, 120f, 1000f);

        var player = new Player(
                (float) Math.toRadians(60f),
                (float) display.getWidth() / display.getHeight(),
                0.1f,
                1000f
        );
        EntityManager.addComponent(player, new TransformationComponent(
                playerStartPosition,
                new Vector3f(0, 0, 0),
                new Vector3f(1f, 1f, 1f)
        ));
        EntityManager.addComponent(player, new PhysicsObjectComponent(new Vector3f(
            PlayerMovementController.WIDTH,
            PlayerMovementController.HEIGHT,
            PlayerMovementController.DEPTH
        )));
        EntityManager.addComponent(player, new PlayerMovementController());
        EntityManager.addComponent(player, new PlayerBlockController());
        EntityManager.addComponent(player, new PlayerMiscController());

        var playerBlockController = EntityManager.getComponent(player, PlayerBlockController.class);

        Keyboard.init(display.getWindow());
        Mouse.init(display,
                () -> playerBlockController.onBreakClicked(player),
                () -> playerBlockController.onBuildClicked(player)
        );

        var renderer = new Renderer();

        // render 1 frame before beginning load, later swap this for loading text
        renderer.render(player);
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
            Mouse.update();
            {
                var transform = EntityManager.getComponent(player, TransformationComponent.class);
                ChunkLoader.update(transform.getPosition());
            }

            TerrainModelLoader.loadChunks();

            // start comp update
            EntityManager.update(); // remove toBeRemoved components.

            EntityManager.updateComponents(PlayerMovementController.class);
            EntityManager.updateComponents(PlayerBlockController.class);
            EntityManager.updateComponents(PlayerMiscController.class);

            EntityManager.updateComponents(ItemComponent.class);

            EntityManager.updateComponents(PhysicsObjectComponent.class);
            EntityManager.updateComponents(TransformationComponent.class);

            // end comp update

            // render
            int verticesRendered = renderer.render(player);
            GLFW.glfwSetWindowTitle(display.getWindow(),
                    verticesRendered/3 + " triangles @" +
                    (int) Timer.getFps() + " fps @" +
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
        Mouse.destroy(display.getWindow());
        GLFW.glfwSetErrorCallback(null).free();
        if (DEBUG) {
            debugMessageCallback.free();
        }


        GL.setCapabilities(null);
        display.destroy();
        GLFW.glfwTerminate();

        System.out.println("Thank you for playing wing commander!");
    }

}
