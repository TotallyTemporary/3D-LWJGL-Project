package render;

public class DefaultTexture {

    public static Texture texture;

    public static void init() {
        texture = new ArrayTexture(
            "src/main/resources/cat_array.png",
            "arrayTexture",
            64, 64
        );
    }

}
