package io;

import render.BasicTexture;
import render.Texture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class MTLFileParser {

    // very simplified .MTL file parser
    // essentially just maps a material name to its texture for now.

    public static synchronized HashMap<String, Texture> loadMTLFile(Path path) {
        String[] lines;
        try {
            String content = Files.readString(path);
            lines = content.split("\n");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not open .mtl file: '" + path + "'");
        }

        HashMap<String, Texture> textures = new HashMap<String, Texture>();

        String materialName = null;

        for (String line : lines) {
            if (line.startsWith("newmtl")) {
                materialName = line.split(" ")[1];
            }

            if (line.startsWith("map_Kd")) {
                if (materialName == null) {
                    System.err.println("Found texture with no material name.");
                }

                String relativePath = line.split(" ")[1];
                Path texturePath = path.getParent().resolve(relativePath);
                Texture texture = new BasicTexture(texturePath.toString(), "basicTexture");
                textures.put(materialName, texture);
            }
        }

        return textures;
    }

}
