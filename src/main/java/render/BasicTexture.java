package render;

import org.lwjgl.opengl.GL30;

public class BasicTexture extends Texture {

    public BasicTexture(String path, String name) {
        super(path, name);
        super.type = GL30.GL_TEXTURE_2D;

        super.id = GL30.glGenTextures();
        GL30.glBindTexture(type, id);

        GL30.glTexImage2D(type, 0, GL30.GL_RGBA8, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, super.imageData);

        GL30.glGenerateMipmap(type);
    }
}
