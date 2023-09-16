package main;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

/** This global object gets a bunch of OpenGL constants about maximum values and other restrictions.
 * These may be used for validation or taking different code paths depending on system requirements. */
public class Capabilities {

    public static int MAX_ARRAY_TEXTURE_LAYERS = -1,
                      MAX_TEXTURE_SIZE         = -1,
                      MAX_MS_SAMPLES           = -1,
                      MAX_AS_DEGREE            = -1;

    public static void get() {
        MAX_ARRAY_TEXTURE_LAYERS = getInt(GL30.GL_MAX_ARRAY_TEXTURE_LAYERS);
        MAX_TEXTURE_SIZE         = getInt(GL30.GL_MAX_TEXTURE_SIZE);
        MAX_MS_SAMPLES           = getInt(GL30.GL_MAX_SAMPLES);
        MAX_AS_DEGREE            = getInt(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
    }

    private static int getInt(int name) {
        try (var stack = MemoryStack.stackPush()) {
            var maxArrayTextureLayers = stack.mallocInt(1);
            GL30.glGetIntegerv(name, maxArrayTextureLayers);
            return maxArrayTextureLayers.get();
        }
    }

}
