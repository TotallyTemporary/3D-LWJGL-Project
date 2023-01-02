package chunk;

import entity.EntityManager;
import entity.ModelComponent;
import entity.TransformationComponent;
import org.joml.Vector3f;
import render.Model;
import render.Texture;
import shader.Shader;

import java.util.ArrayDeque;
import java.util.Queue;

public class TerrainModelLoader {

    private static final int LOAD_LIMIT = 3;
    private static final Queue<Chunk> modelLoadQueue = new ArrayDeque<>();

    public static void addChunk(Chunk chunk) {
        chunk.setStatus(Chunk.Status.MESH_GENERATING);
        modelLoadQueue.add(chunk);
    }

    public static void loadChunks(Shader shader, Texture terrainTexture) {
        Chunk chunk;
        int count = 0;
        while ((chunk = modelLoadQueue.poll()) != null) {
            var chunkModelData = EntityManager.getComponent(chunk, ChunkModelDataComponent.class);
            var model = new Model()
                .addPosition3D(chunkModelData.positions)
                .addTextureCoords3D(chunkModelData.textureCoordinates)
                .setTexture(terrainTexture)
                .setShader(shader)
                .end();
            EntityManager.removeComponent(chunk, chunkModelData);
            EntityManager.addComponent(chunk, new ModelComponent(model));

            var pos = chunk.getChunkPos();
            EntityManager.addComponent(chunk, new TransformationComponent(
                    new Vector3f(pos.x * Chunk.SIZE, pos.y * Chunk.SIZE, pos.z * Chunk.SIZE),
                    new Vector3f(0, 0, 0),
                    1f
            ));

            chunk.setStatus(Chunk.Status.FINAL);

            if (count >= LOAD_LIMIT) break;
            count++;
        }
    }

}
