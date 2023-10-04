package main;

import ai.BasicAIComponent;
import ai.SpinComponent;
import block.Block;
import chunk.*;
import debug.DebugTimer;
import entity.*;
import io.OBJFileParser;
import item.ItemComponent;
import item.ItemModel;
import item.ItemThumbnailRenderer;
import item.ItemType;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.Callback;
import org.lwjgl.system.Configuration;
import player.*;
import render.*;
import shader.Shader;
import ui.UIArrayModelComponent;
import ui.UIModelComponent;

import java.nio.file.Path;

public class Main {

    public static final boolean DEBUG = false;
    public static Callback debugMessageCallback = null;

    // TODO separate into GraphicsSettings object
    private static final int ANISOTROPIC_FILTERING = 8;


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
                false,          // vsync
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
        .addUniform("viewMatrix")
        .addUniform("doCullTransparent");

        var terrainTexture = new ArrayTexture(
                "src/main/resources/blocks_array.png",
                "arrayTexture", 16, 16, ANISOTROPIC_FILTERING);

        TerrainModelLoader.setChunkTexture(terrainTexture);
        TerrainModelLoader.setShader(terrainShader);

        var itemsShader = new Shader(
            "src/main/resources/shaders/item_vertex.glsl",
            "src/main/resources/shaders/item_fragment.glsl"
        )
        .addUniform("transformationMatrix")
        .addUniform("projectionMatrix")
        .addUniform("viewMatrix");

        var blockBreakShader = new Shader(
                "src/main/resources/shaders/breaking_vertex.glsl",
                "src/main/resources/shaders/breaking_fragment.glsl"
        )
                .addUniform("transformationMatrix")
                .addUniform("projectionMatrix")
                .addUniform("viewMatrix")
                .addUniform("textureIndex");

        var blockSelectionShader = new Shader(
                "src/main/resources/shaders/selection_vertex.glsl",
                "src/main/resources/shaders/selection_fragment.glsl"
        )
                .addUniform("transformationMatrix")
                .addUniform("projectionMatrix")
                .addUniform("viewMatrix")
                .addUniform("colourMultiplier");

        BlockSelection.setShader(blockSelectionShader);
        BlockSelection.setTexture(terrainTexture);
        Block.createBreakModels(terrainTexture, blockBreakShader);

        var itemsTexture = new ArrayTexture(
            "src/main/resources/items_array.png",
            "arrayTexture", 16, 16, ANISOTROPIC_FILTERING);

        ItemModel.init(itemsShader, terrainTexture, itemsTexture);
        DefaultTexture.init();

        UIModelComponent.setUISettings(display);
        UIArrayModelComponent.createUIModels(display);

        var mobShader = new Shader(
                "src/main/resources/shaders/mobile_vertex.glsl",
                "src/main/resources/shaders/mobile_fragment.glsl"
        )
        .addUniform("transformationMatrix")
        .addUniform("projectionMatrix")
        .addUniform("viewMatrix");

        OBJFileParser.setShader(mobShader);

        var crosshair = new Entity();
        EntityManager.addComponent(crosshair, new UIArrayModelComponent(15));
        EntityManager.addComponent(crosshair, new TransformationComponent(new Vector3f(), new Vector3f(), new Vector3f(0.05f, 0.05f, 0.05f)));

        var loadingScreenIcon = new Entity();
        Texture grassIcon = ItemThumbnailRenderer.renderItem(ItemType.GRASS.getID());
        EntityManager.addComponent(loadingScreenIcon, new UIModelComponent(grassIcon));
        EntityManager.addComponent(loadingScreenIcon, new TransformationComponent(new Vector3f(), new Vector3f(), new Vector3f(0.25f, 0.25f, 0.25f)));

        var playerStartPosition = new Vector3f(1000f, 105f, 1000f);

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
        EntityManager.addComponent(player, new PlayerInventoryController());

        Keyboard.init(display.getWindow());
        Mouse.init(display);

        // create maxwell
        var maxwellModel = OBJFileParser.loadModel(Path.of("src/main/resources/mobs/maxwell/maxwell.obj"));
        Entity maxwell = new Entity();
        EntityManager.addComponent(maxwell, maxwellModel);
        EntityManager.addComponent(maxwell, new TransformationComponent(
                playerStartPosition.add(new Vector3f(0, 5, 0), new Vector3f()),
                new Vector3f(0, 0, 0),
                new Vector3f(1f / 16, 1f / 16, 1f / 16)));
        EntityManager.addComponent(maxwell, new BasicAIComponent());
        EntityManager.addComponent(maxwell, new PhysicsObjectComponent(new Vector3f(1.25f, 1.25f, 1.25f)));
        EntityManager.addComponent(maxwell, new SpinComponent());

        var renderer = new Renderer();

        // render 1 frame before beginning load, later swap this for loading text
        renderer.render(player);
        GLFW.glfwSwapBuffers(display.getWindow());

        // remove loading screen icon
        EntityManager.removeEntity(loadingScreenIcon);

        System.out.println("Starting world preload");
        ChunkLoader.start(playerStartPosition);
        while (ChunkLoader.update(playerStartPosition) > 0 || ChunkLoader.getQueueSize() > 0) {
            TerrainModelLoader.loadChunks(Integer.MAX_VALUE);
            display.setTitle(0);
        }

        System.out.println("Chunks loaded");

        // make small safe area around player
        for (int dx = -3; dx < 4; dx++)
        for (int dy = -3; dy < 4; dy++)
        for (int dz = -3; dz < 4; dz++)
        {
            Vector3i pos = new Vector3i(
                    (int) playerStartPosition.x,
                    (int) playerStartPosition.y,
                    (int) playerStartPosition.z
            ).add(new Vector3i(dx, dy, dz));
            ChunkLoader.setBlockAt(pos, Block.AIR.getID());
        }

        while (!GLFW.glfwWindowShouldClose(display.getWindow())) {
            // press K for wireframe
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_K)) {
                GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_LINE);
            } else {
                GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_FILL);
            }

            // update
            Timer.tick();
            DebugTimer.clear();
            Mouse.update();
            EntityManager.update();

            // these are less risky after chunk loading is done and entitymanager is updated
            // so component changes are less likely to muck us up
            ChunkLoader.updatePriorityChunks();
            TerrainModelLoader.loadChunks();

            // this is our component update order
            EntityManager.updateComponents(PlayerMovementController.class);
            EntityManager.updateComponents(PlayerBlockController.class);
            EntityManager.updateComponents(PlayerMiscController.class);
            EntityManager.updateComponents(BasicAIComponent.class);

            EntityManager.updateComponents(ItemComponent.class);

            EntityManager.updateComponents(PhysicsObjectComponent.class);
            EntityManager.updateComponents(TransformationComponent.class);

            EntityManager.updateComponents(SpinComponent.class);

            // end comp update

            // start chunk loader
            {
                var transform = EntityManager.getComponent(player, TransformationComponent.class);
                ChunkLoader.startUpdate(transform.getPosition());
            }

            // render
            int verticesRendered = renderer.render(player);
            display.setTitle(verticesRendered);

            // glfw stuff
            GLFW.glfwSwapBuffers(display.getWindow());
            GLFW.glfwPollEvents();

            ChunkLoader.endUpdate();
        }

        TerrainGenerator.stop();
        StructureGenerator.stop();
        LightMapGenerator.stop();
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
