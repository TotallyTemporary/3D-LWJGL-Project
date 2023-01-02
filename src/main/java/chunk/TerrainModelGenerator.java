package chunk;

import entity.EntityManager;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;

public class TerrainModelGenerator {

    private static final Queue<Chunk> chuckLoadQueue = new ArrayDeque<>();

    public static void addChunk(Chunk chunk) {
        chunk.setStatus(Chunk.Status.MESH_GENERATING);
        chuckLoadQueue.add(chunk);
    }

    public static void loadChunks() {
        Chunk chunk;
        while ((chunk = chuckLoadQueue.poll()) != null) {
            generateModelData(chunk);
            chunk.setStatus(Chunk.Status.PREPARED);
        }
    }

    private static void generateModelData(Chunk chunk) {
        List<Float> verticesBuffer = new ArrayList<>();
        List<Float> textureCoordsBuffer = new ArrayList<>();
        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++) {
            var block = Block.getBlock(chunk.getBlock(x, y, z));
            for (int index = 0; index < 6; index++) {
                var face = block.getFace(index);
                if (!isFaceVisible(chunk, face, x, y, z)) continue;

                var vertices = to3DVectors(face.getVertices());
                for (var vertex : vertices) {
                    verticesBuffer.add(vertex.x + x);
                    verticesBuffer.add(vertex.y + y);
                    verticesBuffer.add(vertex.z + z);
                }

                for (var textureCoord : face.getTextureCoords()) {
                    textureCoordsBuffer.add(textureCoord);
                }
            }
        }

        EntityManager.addComponent(chunk, new ChunkModelDataComponent(
                toPrimitive(verticesBuffer),
                toPrimitive(textureCoordsBuffer)
        ));
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

    private static boolean isFaceVisible(Chunk chunk, BlockFace face, int x, int y, int z) {
        var obscuringBlock = switch (face.direction) {
            case UP    -> getBlockSafe(chunk, new Vector3i(x, y+1, z));
            case DOWN  -> getBlockSafe(chunk, new Vector3i(x, y-1, z));
            case LEFT  -> getBlockSafe(chunk, new Vector3i(x-1, y, z));
            case RIGHT -> getBlockSafe(chunk, new Vector3i(x+1, y, z));
            case FRONT -> getBlockSafe(chunk, new Vector3i(x, y, z-1));
            case BACK  -> getBlockSafe(chunk, new Vector3i(x, y, z+1));
        };
        var oppositeDirection = face.direction.opposite().ordinal();
        if (!Block.getBlock(obscuringBlock).getFace(oppositeDirection).isTransparent()) {
            return false;
        }

        return true;
    }

    private static byte getBlockSafe(Chunk chunk, Vector3i pos) {
        var block = chunk.getBlock(pos);
        if (block == Block.INVALID.getID()) {
            var worldPos = Chunk.blockPosToWorldPos(pos, chunk);
            block = ChunkLoader.getBlockAt(worldPos);
        }
        return block;
    }

}
