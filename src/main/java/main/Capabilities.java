package main;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public class Capabilities {

    public static int MAX_ARRAY_TEXTURE_LAYERS = -1,
                      MAX_TEXTURE_SIZE         = -1,
                      MAX_MS_SAMPLES           = -1;

    public static void get() {
        MAX_ARRAY_TEXTURE_LAYERS = getInt(GL30.GL_MAX_ARRAY_TEXTURE_LAYERS);
        MAX_TEXTURE_SIZE          = getInt(GL30.GL_MAX_TEXTURE_SIZE);
        MAX_MS_SAMPLES           = getInt(GL30.GL_MAX_SAMPLES);
    }

    private static int getInt(int name) {
        try (var stack = MemoryStack.stackPush()) {
            var maxArrayTextureLayers = stack.mallocInt(1);
            GL30.glGetIntegerv(name, maxArrayTextureLayers);
            return maxArrayTextureLayers.get();
        }
    }

}
