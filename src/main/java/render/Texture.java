package render;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.BufferUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class Texture {

    protected int id;
    protected int type;

    protected String path;
    protected String name;
    protected int width, height;
    protected ByteBuffer imageData;
    public Texture(String path, String name) {
        this.path = path;
        this.name = name;

        var BYTES_PER_PIXEL = 4;
        var FORMAT = PNGDecoder.Format.RGBA;

        ByteBuffer buffer = null;
        try {
            var fis = new FileInputStream(path);
            var decoder = new PNGDecoder(fis);
            this.width = decoder.getWidth();
            this.height = decoder.getHeight();

            buffer = BufferUtils.createByteBuffer(BYTES_PER_PIXEL * width * height);
            decoder.decode(buffer, width * BYTES_PER_PIXEL, FORMAT);
        } catch (FileNotFoundException e) {
            System.err.println("Texture file not found: " + path);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not open texture file: " + path);
            e.printStackTrace();
        }

        this.imageData = buffer.flip();
    }

    public int getTexture() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }


}