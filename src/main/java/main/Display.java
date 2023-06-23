package main;

import chunk.ChunkLoader;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class Display {

    /**
     * Represents the display mode of a window.
     * @apiNote FULLSCREEN_BORDERLESS ignores passed resolution and uses the resolution of the monitor.
     * @apiNote FULLSCREEN can be considered "sluggish" as it has to change the resolution of the monitor to match.
     */
    public enum DisplayMode {
        FULLSCREEN, WINDOWED, FULLSCREEN_BORDERLESS
    }

    /**
     * Represents the settings passed to a Display to generate a window.
     * @param title Displayed at the top of the window.
     * @param width Horizontal resolution
     * @param height Vertical resolution
     * @param monitor Index of monitor to launch this window on, pass -1 to choose primary monitor.
     * @param vsync  true=enable vsync, false=disable vsync
     * @param displayMode chooses displaymode for this window.
     * @see DisplayMode
     */
    public record DisplaySettings (
        String title,
        int width, int height,
        long monitor,
        boolean vsync,
        DisplayMode displayMode
    ) { }

    private long window;
    private int width, height;
    private long monitor;
    private DisplayMode mode;

    public Display(DisplaySettings settings) {
        // unpack settings ; these values will be updated as we go.
        int width = settings.width, height = settings.height;
        var monitor = settings.monitor;
        var mode = settings.displayMode;

        if (!GLFW.glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

        // get monitor, width and height settings.
        if (monitor == -1) monitor = GLFW.glfwGetPrimaryMonitor();
        var vidMode = GLFW.glfwGetVideoMode(monitor);

        // for fullscreen borderless, the width, height of the window must match vidmode.
        // also see we're setting window hints to match vidMode.
        if (width == -1 || height == -1 || mode == DisplayMode.FULLSCREEN_BORDERLESS) {
            width = vidMode.width();
            height = vidMode.height();
        }

        if (mode == DisplayMode.FULLSCREEN_BORDERLESS) {
            GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, vidMode.redBits());
            GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, vidMode.greenBits());
            GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, vidMode.blueBits());
            GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, vidMode.refreshRate());
        }

        // don't pass in a monitor if we want a windowed display.
        if (mode == DisplayMode.WINDOWED) {
            monitor = 0;
        }

        if (Main.DEBUG) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        }

        long window = GLFW.glfwCreateWindow(width, height, settings.title, monitor, 0);
        if (window == 0) {
            throw new IllegalStateException("Could not create a GLFW window.");
        }

        // center window
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer wWidth = stack.mallocInt(1);
            IntBuffer wHeight = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(window, wWidth, wHeight);

            int mWidth = vidMode.width();
            int mHeight = vidMode.height();

            GLFW.glfwSetWindowPos(window, (mWidth - wWidth.get()) / 2, (mHeight - wHeight.get()) / 2);
        }
        GLFW.glfwShowWindow(window);
        GLFW.glfwMakeContextCurrent(window);

        GL.createCapabilities();
        GL30.glViewport(0, 0, width, height);

        if (Main.DEBUG) {
            Main.debugMessageCallback = GLUtil.setupDebugMessageCallback(System.out);
        }

        int swapInterval = settings.vsync ? 1 : 0;
        GLFW.glfwSwapInterval(swapInterval);

        System.out.println("Created window at " + width + "x" + height);
        this.width = width;
        this.height = height;
        this.monitor = monitor;
        this.window = window;
        this.mode = mode;
    }

    public void setTitle(int verticesRendered) {
        GLFW.glfwSetWindowTitle(getWindow(),
                verticesRendered/3 + " triangles : " +
                        Timer.getFps() + " fps : " +
                        "%.2f".formatted(Timer.getFrametimeMillis()) + " ms : " +
                        ChunkLoader.getQueueSize() + " chunks queued");
    }

    public void close() {
        GLFW.glfwSetWindowShouldClose(window, true);
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(window);
    }

    public long getWindow() {
        return window;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getMonitor() {
        return monitor;
    }

    public DisplayMode getDisplayMode() {
        return mode;
    }
}
