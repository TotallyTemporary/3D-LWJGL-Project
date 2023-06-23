package item;

import org.lwjgl.opengl.GL30;
import render.Texture;

public class ItemThumbnailTexture extends Texture {
    public ItemThumbnailTexture(int id) {
        super(id, GL30.GL_TEXTURE_2D, "texie");
    }
}
