package chunk;

import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import render.Model;
import render.Texture;
import shader.Shader;

import java.util.ArrayDeque;
import java.util.Queue;

public class TerrainModelLoader {

    // opengl models can only be loaded on the main thread, so no multithreading here.
    // if you want to be fancy, you could stream the data in though...

    private static final int DEFAULT_LOAD_LIMIT = 3;
    private static final Queue<Chunk> modelLoadQueue = new ArrayDeque<>();

    private static Shader chunkShader = null;
    private static Texture chunkTexture = null;


    public static void addChunk(Chunk chunk) {
        modelLoadQueue.add(chunk);
    }

    public static void loadChunks() {
        loadChunks(DEFAULT_LOAD_LIMIT);
    }

    public static void loadChunks(int loadLimit) {
        int count = 0;
        var it = modelLoadQueue.iterator();
        while (it.hasNext()) {
            var chunk = it.next();
            loadChunk(chunk);
            it.remove();
            if (count++ >= loadLimit) break;
        }
    }

    public static int getQueueSize() {
        return modelLoadQueue.size();
    }

    public static void loadChunk(Chunk chunk) {
        assert EntityManager.hasComponent(chunk, ChunkModelDataComponent.class);

        var chunkModelData = EntityManager.removeComponent(chunk, ChunkModelDataComponent.class);

        if (chunkModelData.positions.length != 0) {
            var pos = chunk.getChunkPos();
            EntityManager.addComponent(chunk, new TransformationComponent(
                    new Vector3f(pos.x * Chunk.SIZE, pos.y * Chunk.SIZE, pos.z * Chunk.SIZE),
                    new Vector3f(0, 0, 0),
                    new Vector3f(1, 1, 1)
            ));

            var model = new Model()
                .addPosition3D(chunkModelData.positions)
                .addTextureCoords3D(chunkModelData.textureCoordinates)
                .setTexture(chunkTexture)
                .setShader(chunkShader)
                .end();
            EntityManager.addComponent(chunk, new ChunkModelComponent(model, chunkModelData.positionsIndices));
        }

        chunk.setStatus(Chunk.Status.FINAL);
    }

    public static void setShader(Shader shader) {
        chunkShader = shader;
    }

    public static void setChunkTexture(Texture texture) {
        chunkTexture = texture;
    }

}
