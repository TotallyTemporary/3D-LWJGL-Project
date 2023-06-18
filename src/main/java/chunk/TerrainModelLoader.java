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

    private static final int DEFAULT_LOAD_LIMIT = 20;
    private static final int HIGH_LOAD_LIMIT = 200; // if we've got a large queue, increase limit

    private static final Queue<Chunk> modelLoadQueue = new ArrayDeque<>();

    private static Shader chunkShader = null;
    private static Texture chunkTexture = null;


    public static void addChunk(Chunk chunk) {
        modelLoadQueue.add(chunk);
    }

    public static void loadChunks() {
        var limit = getQueueSize() > HIGH_LOAD_LIMIT ? HIGH_LOAD_LIMIT : DEFAULT_LOAD_LIMIT;
        loadChunks(limit);
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
        // previous step should have given the chunk its model data as a component
        assert EntityManager.hasComponent(chunk, ChunkModelDataComponent.class);

        // set get this component and remove it
        var chunkModelData = EntityManager.removeComponent(chunk, ChunkModelDataComponent.class);

        // if the chunk already has a loaded model, we unload it. This might fix a memory leak.
        var chunkModelComponent = EntityManager.removeComponent(chunk, ChunkModelComponent.class);
        if (chunkModelComponent != null) {
            chunkModelComponent.getModel().destroy();
        }

        if (chunkModelData.positions.length != 0) {
            var pos = chunk.getChunkGridPos();
            EntityManager.addComponent(chunk, new TransformationComponent(
                    new Vector3f(pos.x * Chunk.SIZE, pos.y * Chunk.SIZE, pos.z * Chunk.SIZE),
                    new Vector3f(0, 0, 0),
                    new Vector3f(1, 1, 1)
            ));

            var model = new Model()
                .addPosition3D(chunkModelData.positions)
                .addTextureCoords3D(chunkModelData.textureCoordinates)
                .addLight2D(chunkModelData.light)
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
