package chunk;

import block.CraftingTableBlockEntity;
import entities.EntityType;
import entities.ItemEntity;
import entities.MaxwellEntity;
import entity.*;
import org.joml.Vector3i;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ChunkSerializer {

    private static final Path path = Path.of("src/main/resources/world");

    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public static boolean doesSaveExist(Chunk chunk) {
        var file = chunkToFile(chunk);
        return file.exists();
    }

    public static int getQueueSize() {
        return executor.getQueue().size();
    }

    public static void stop() {
        executor.shutdown(); // blocks until all executed
    }

    public static void serialize(Chunk chunk) {
        // first get the entities, this data should be fairly small.
        List<Entity> entities = getChunkEntities(chunk);
        byte[] entityData;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            writeEntities(dos, entities);
            dos.close();
            entityData = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // then on the worker thread, we write the actual chunk block data to the file
        executor.submit(() -> {
            try {
                serializeChunk(chunk, entityData);
                chunk.setStatus(Chunk.Status.NONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void serializeChunk(Chunk chunk, byte[] entityData) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(chunkToFile(chunk)))) {
            // write block data
            out.writeBoolean(chunk.getIsAirChunk());
            byte[] blocks = chunk.getBlocks();
            if (chunk.getIsAirChunk()) {
                blocks = new byte[Chunk.SIZE*Chunk.SIZE*Chunk.SIZE];
            }
            out.write(blocks);

            // write entities
            out.write(entityData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Entity> getChunkEntities(Chunk chunk) {
        List<Entity> chunkEntities = new ArrayList<>();

        var entities = EntityManager.getComponents(SerializableEntityComponent.class).keySet();
        for (Entity entity : entities) {
            var transform = EntityManager.getComponent(entity, TransformationComponent.class);
            var position = transform.getPosition();
            var chunkPosition = Chunk.worldPosToChunkPos(position);
            if (chunkPosition.equals(chunk.getChunkGridPos())) {
                chunkEntities.add(entity);
            }
        }

        return chunkEntities;
    }

    private static void writeEntities(DataOutputStream out, List<Entity> serializeEntities) throws IOException {
        for (Entity entity : serializeEntities) {
            var cEntity = (SerializableEntity) entity;
            out.writeInt(cEntity.getType());
            cEntity.serialize(out);
            EntityManager.removeEntitySafe(entity);
        }
    }

    public static void deserialize(Chunk chunk) {
        executor.submit(() -> {
            try {
                deserializeChunk(chunk);
                chunk.setStatus(Chunk.Status.BLOCKS_GENERATED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void deserializeChunk(Chunk chunk) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(chunkToFile(chunk)))) {
            boolean isAir = in.readBoolean();
            byte[] blocks = in.readNBytes(Chunk.SIZE * Chunk.SIZE * Chunk.SIZE);
            chunk.setBlocks(blocks);
            chunk.setColours(new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE]);
            chunk.setIsAirChunk(isAir);
            readEntities(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readEntities(DataInputStream in) throws IOException {
        while (in.available() > 0) {
            int type = in.readInt();
            SerializableEntity entity = initializeEntity(type);
            entity.deserialize(in);
        }
    }

    private static SerializableEntity initializeEntity(int type) {
        switch (type) {
            case EntityType.MAXWELL: return new MaxwellEntity();
            case EntityType.CRAFTING_TABLE: return new CraftingTableBlockEntity();
            case EntityType.ITEM: return new ItemEntity();
            default: throw new IllegalArgumentException("Bad entity type: " + type);
        }
    }

    private static File chunkToFile(Chunk chunk) {
        var pos = chunk.getChunkGridPos();
        return path.resolve("chunk_" + pos.x + "_" + pos.y + "_" + pos.z + ".dat").toFile();
    }

}
