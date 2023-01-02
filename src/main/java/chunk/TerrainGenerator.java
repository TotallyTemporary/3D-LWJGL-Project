package chunk;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TerrainGenerator {

    public static void loadChunk(Chunk chunk) {
        generateTerrain(chunk);
        generateModelData(chunk);
        chunk.setStatus(Chunk.Status.WAIT_NEIGHBORS);
    }

    private static void generateTerrain(Chunk chunk) {
        // generate blocks
        byte[][][] blocks = new byte[Chunk.SIZE][Chunk.SIZE][Chunk.SIZE];

        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++)
        {
            blocks[x][y][z] = Block.AIR.getID();
        }

        blocks[5][5][5] = Block.STONE.getID();
        blocks[5][6][5] = Block.STONE.getID();

        chunk.setBlocks(blocks);
    }

    private static void generateModelData(Chunk chunk) {
        var blocks = chunk.getBlocks();
        List<Float> verticesBuffer = new ArrayList<>();
        List<Float> textureCoordsBuffer = new ArrayList<>();
        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++) {
            var block = Block.getBlock(blocks[x][y][z]);
            if (block == Block.AIR) continue;
            for (int face = 0; face < 6; face++) {
                var vertices = to3DVectors(block.getFace(face).getVertices());
                for (var vertex : vertices) {
                    verticesBuffer.add(vertex.x + x);
                    verticesBuffer.add(vertex.y + y);
                    verticesBuffer.add(vertex.z + z);
                }

                for (var textureCoord : block.getFace(face).getTextureCoords()) {
                    textureCoordsBuffer.add(textureCoord);
                }
            }
        }

        chunk.setModel(toPrimitive(verticesBuffer),
                       toPrimitive(textureCoordsBuffer));
    }

    private static ArrayList<Vector3f> to3DVectors(float[] somePositions) {
        var lst = new ArrayList<Vector3f>();
        for (int i = 0; i < somePositions.length/3; i++) {
            lst.add(new Vector3f(
                    somePositions[i*3  ],
                    somePositions[i*3+1],
                    somePositions[i*3+2]
            ));
        }

        return lst;
    }

    private static float[] toPrimitive(Collection<Float> coll) {
        var ret = new float[coll.size()];
        int i = 0;
        for (float f : coll) {
            ret[i++] = f;
        }
        return ret;
    }

}
