package io;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import render.Model;
import render.Texture;
import shader.Shader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class OBJFileParser {

    // obj file at:
    // "src/main/resources/mobs/maxwell/maxwell.obj"
    // mat file at:
    // "src/main/resources/mobs/maxwell/maxwell.mtl"
    // tex file at:
    // "src/main/resources/mobs/maxwell/textures/dingus_baseColor.jpeg"

    private static Shader shader;

    // accumulated over the whole file
    private static ArrayList<Vector3f> vertices = new ArrayList<>();
    private static ArrayList<Vector2f> textureCoords = new ArrayList<>();
    private static ArrayList<Vector3f> normals = new ArrayList<>();

    // gathered from faces
    private static FloatArrayList positionsBuffer = new FloatArrayList();
    private static FloatArrayList textureCoordsBuffer = new FloatArrayList();

    private static HashMap<String, Texture> textures = new HashMap<>();
    private static ArrayList<MultitextureModelPart> parts = new ArrayList<>();

    public static void setShader(Shader newShader) {
        shader = newShader;
    }

    public static synchronized MultitextureModelComponent loadModel(Path path) {
        // TODO clear

        // read file lines
        String[] lines;
        try {
            String content = Files.readString(path);
            lines = content.split("\n");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not open .obj file: '" + path + "'");
        }

        // state
        int partVertexCount = 0;
        int partStartVertex = 0;
        Texture currentTexture = null;

        for (String line : lines) {
            if (line.startsWith("v ")) {
                // vertices
                String[] split = line.split(" ");
                float x = Float.parseFloat(split[1]);
                float y = Float.parseFloat(split[2]);
                float z = Float.parseFloat(split[3]);
                vertices.add(new Vector3f(x, y, z));
            } else if (line.startsWith("vt ")) {
                // texture coords
                String[] split = line.split(" ");
                float u = Float.parseFloat(split[1]);
                float v = Float.parseFloat(split[2]);
                textureCoords.add(new Vector2f(u, v));
            } else if (line.startsWith("vn ")) {
                // normals
                String[] split = line.split(" ");
                float x = Float.parseFloat(split[1]);
                float y = Float.parseFloat(split[2]);
                float z = Float.parseFloat(split[3]);
                normals.add(new Vector3f(x, y, z));
            } else if (line.startsWith("o ")) {
                // ignore
            } else if (line.startsWith("s ")) {
                // ignore
            } else if (line.startsWith("f ")) {
                // faces
                // f 100/200/300 400/500/600, where 100 and 400 are indices to vertex positions, etc.
                // note: starts at 1 :(
                String[] split = line.split(" ");
                for (int i = 1; i < split.length; i++) {
                    String vertex = split[i];
                    String[] attributes = vertex.split("/");

                    Vector3f vertexPosition = vertices.get(Integer.parseInt(attributes[0]) - 1);
                    Vector2f vertexTextureCoords = textureCoords.get(Integer.parseInt(attributes[1]) - 1);
                    Vector3f vertexNormals = normals.get(Integer.parseInt(attributes[2]) - 1);

                    positionsBuffer.add(vertexPosition.x);
                    positionsBuffer.add(vertexPosition.y);
                    positionsBuffer.add(vertexPosition.z);

                    textureCoordsBuffer.add(vertexTextureCoords.x);
                    textureCoordsBuffer.add(1f - vertexTextureCoords.y); // blender flips y-coordinate

                    partVertexCount += 1;
                }

            } else if (line.startsWith("mtllib")) {
                // import material file
                String fileName = line.split(" ")[1];
                Path directory = path.getParent();
                Path matFile = directory.resolve(fileName);

                HashMap<String, Texture> materialTextures = MTLFileParser.loadMTLFile(matFile.toAbsolutePath());
                for (var entry : materialTextures.entrySet()) {
                    textures.put(entry.getKey(), entry.getValue());
                }
            } else if (line.startsWith("usemtl")) {
                // use material definition (texture)
                String name = line.split(" ")[1];

                // if this is the end of a previous part, add the previous part definition
                if (partVertexCount != 0) {
                    parts.add(new MultitextureModelPart(currentTexture, partStartVertex, partVertexCount));
                }

                currentTexture = textures.get(name);
                partStartVertex = partStartVertex + partVertexCount;
                partVertexCount = 0;
            }
        }

        // for the last faces add, add the part for them
        parts.add(new MultitextureModelPart(currentTexture, partStartVertex, partVertexCount));

        Model model = new Model()
                .addPosition3D(positionsBuffer.elements())
                .addTextureCoords2D(textureCoordsBuffer.elements())
                .setShader(shader);
        for (Texture texture : textures.values()) {
            model.setTexture(texture);
        }
        model.end();

        return new MultitextureModelComponent(model, parts.toArray(new MultitextureModelPart[0]));
    }

}
