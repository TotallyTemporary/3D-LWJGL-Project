package chunk;

import entity.EntityManager;
import entity.ModelComponent;
import entity.TestSpinComponent;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;
import render.Model;
import render.Texture;
import shader.Shader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class ChunkLoader {

    private Shader shader;
    private Texture terrainTexture;

    private int loadRadius = 3;
    private HashMap<Vector3i, Chunk> chunks = new HashMap<>();

    public ChunkLoader(Shader shader, Texture terrainTexture) {
        this.shader = shader;
        this.terrainTexture = terrainTexture;
    }

    public int update(Vector3f playerPos) {
        Vector3i playerChunkPos = Chunk.worldPosToChunkPos(playerPos);
        int chunkX = playerChunkPos.x;
        int chunkY = playerChunkPos.y;
        int chunkZ = playerChunkPos.z;

        int updatedCount = 0;
        for (int x = chunkX-loadRadius; x < chunkX+loadRadius+1; x++)
        for (int y = chunkY-loadRadius; y < chunkY+loadRadius+1; y++)
        for (int z = chunkZ-loadRadius; z < chunkZ+loadRadius+1; z++)
        {
            var pos = new Vector3i(x, y, z);
            var status = getStatus(pos);
            switch (status) {
                case NONE           -> {
                    startTerrainGen(pos);
                    updatedCount++;
                }
                case WAIT_NEIGHBORS -> {
                    var updated = doNeighborCheck(pos);
                    if (updated) {
                        updatedCount++;
                    }
                }
            }
        }

        return updatedCount;
    }

    private Chunk.Status getStatus(Vector3i pos) {
        var chunk = chunks.get(pos);
        if (chunk == null) return Chunk.Status.NONE;
        else return chunk.getStatus();
    }

    private void startTerrainGen(Vector3i pos) {
        // initalize chunk if needed
        var chunk = chunks.get(pos);
        if (chunk == null) {
            chunk = new Chunk(pos);
            chunks.put(pos, chunk);
        }

        // place chunk into terraingen queue, this is preparing for multithreading.
        chunk.setStatus(Chunk.Status.TERRAIN_GEN);
        TerrainGenerator.loadChunk(chunk);
    }

    private boolean doNeighborCheck(Vector3i pos) {
        for (var neighbor : neighbors(pos)) {
            // terrain generating or no chunk.
            if (getStatus(neighbor).urgency <= Chunk.Status.TERRAIN_GEN.urgency) {
                return false;
            }
        }
        loadChunkModel(pos);
        return true;
    }

    private void loadChunkModel(Vector3i pos) {
        var chunk = chunks.get(pos);
        chunk.setStatus(Chunk.Status.LOADING);

        var model = new Model()
                .addPosition3D(chunk.getPositions())
                .addTextureCoords3D(chunk.getTextureCoords())
                .setTexture(terrainTexture)
                .setShader(shader)
                .end();
        EntityManager.addComponent(chunk, new ModelComponent(model));

        EntityManager.addComponent(chunk, new TransformationComponent(
                new Vector3f(pos.x * Chunk.SIZE, pos.y * Chunk.SIZE, pos.z * Chunk.SIZE),
                new Vector3f(0, 0, 0),
                1f
        ));
    }

    private Vector3i[] neighbors(Vector3i pos) {
        return new Vector3i[]{
            new Vector3i(pos.x+1, pos.y, pos.z),
            new Vector3i(pos.x-1, pos.y, pos.z),
            new Vector3i(pos.x, pos.y+1, pos.z),
            new Vector3i(pos.x, pos.y-1, pos.z),
            new Vector3i(pos.x, pos.y, pos.z+1),
            new Vector3i(pos.x, pos.y, pos.z-1)
        };
    }

}
