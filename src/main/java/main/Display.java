package main;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class Display {

    public enum DisplayMode {
        FULLSCREEN, WINDOWED, FULLSCREEN_BORDERLESS
    }

    private long window;
    private int width, height;
    private long monitor;

    public Display(String title, int width, int height, long monitor, boolean vsync, DisplayMode mode) {
        var NULL = 0;

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
            monitor = NULL;
        }

        long window = GLFW.glfwCreateWindow(width, height, title, monitor, NULL);
        if (window == NULL) {
            throw new IllegalStateException("Could not create a GLFW window.");
        }

        // center window on monitor
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // get window width and height
            IntBuffer wWidth = stack.mallocInt(1);
            IntBuffer wHeight = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(window, wWidth, wHeight);

            // get monitor width and height
            int mWidth = vidMode.width();
            int mHeight = vidMode.height();

            // center window on monitor
            GLFW.glfwSetWindowPos(window, (mWidth - wWidth.get()) / 2, (mHeight - wHeight.get()) / 2);
        }
        GLFW.glfwShowWindow(window);
        GLFW.glfwMakeContextCurrent(window);

        GL.createCapabilities();
        GL30.glViewport(0, 0, width, height);

        int swapInterval = vsync ? 1 : 0;
        GLFW.glfwSwapInterval(swapInterval);

        this.width = width;
        this.height = height;
        this.monitor = monitor;
        this.window = window;
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
}
