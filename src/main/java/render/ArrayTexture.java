package render;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

public class ArrayTexture extends Texture {

    /* Thank you: http://gaarlicbread.com/post/gl_2d_array */
    public ArrayTexture(String path, String name, int tileWidth, int tileHeight) {
        super(path, name);
        super.type = GL30.GL_TEXTURE_2D_ARRAY;

        // assume one long texture.
        assert tileWidth == super.width;
        int depth = super.height / tileHeight;

        super.id = GL30.glGenTextures();
        GL30.glBindTexture(type, id);

        // set texture params before loading texture
        // when too small to fit on screen, use trilinear (blurry) filtering
        GL30.glTexParameteri(type,
                GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_LINEAR);

        // when close up, pixels should be pixelated :D
        GL30.glTexParameteri(type,
                GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);

        // repeat so we don't get block corners being black or something.
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);

        // I really wanted to do this with <gl3.0 stuff, but I couldn't get array textures
        // working without having to resort to gl4.3. :(
        GL43.glTexStorage3D(
                type,
                1, // mipmaps
                GL30.GL_RGBA8, // internal format
                tileWidth, tileHeight,
                depth
        );

        GL43.glTexSubImage3D(
                type,
                0,    // mipmap index
                0, 0, 0,    // xyz index
                tileWidth, tileHeight, depth,
                GL30.GL_RGBA,
                GL30.GL_UNSIGNED_BYTE,
                super.imageData
        );

        GL30.glGenerateMipmap(type);
    }
}
