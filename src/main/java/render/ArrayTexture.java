package render;

import main.Capabilities;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

public class ArrayTexture extends Texture {

    private int tileWidth, tileHeight;
    private int depth;

    /* Thank you: http://gaarlicbread.com/post/gl_2d_array */
    public ArrayTexture(String path, String name, int tileWidth, int tileHeight, int anisotropicFiltering) {
        super(path, name);
        super.type = GL30.GL_TEXTURE_2D_ARRAY;

        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        // assume one long texture.
        assert tileWidth == super.width;
        depth = super.height / tileHeight;

        if (depth > Capabilities.MAX_ARRAY_TEXTURE_LAYERS) {
            System.err.println("Trying to make array texture of depth " + depth
                    + " but maximum depth is set to " + Capabilities.MAX_ARRAY_TEXTURE_LAYERS);
        }

        if (width > Capabilities.MAX_TEXTURE_SIZE
        || height > Capabilities.MAX_TEXTURE_SIZE) {
            System.err.println("Trying to make texture of size " + width + "x" + height
                        + " but maximum size is " + Capabilities.MAX_TEXTURE_SIZE);
        }

        if (anisotropicFiltering != -1 && anisotropicFiltering > Capabilities.MAX_AS_DEGREE) {
            System.err.println("Trying to make texture of AS degree "
                    + anisotropicFiltering + " but maximum degree is "
                    + Capabilities.MAX_AS_DEGREE);
        }

        super.id = GL30.glGenTextures();
        GL30.glBindTexture(type, id);

        // set texture params before loading texture
        // when too small to fit on screen, use trilinear (blurry) filtering
        GL30.glTexParameteri(type,
                GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_LINEAR);

        // when close up, pixels should be pixelated :D
        GL30.glTexParameteri(type,
                GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);

        // use anisotropic filtering if possible
        if (anisotropicFiltering != -1) {
            GL30.glTexParameteri(
                type,
                EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                anisotropicFiltering
            );
        }

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

    public int getDepth() {
        return depth;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

}
