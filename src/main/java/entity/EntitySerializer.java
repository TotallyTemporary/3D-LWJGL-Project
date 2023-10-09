package entity;

import entities.EntityType;
import entities.MaxwellEntity;

import java.io.*;
import java.nio.file.Path;

public class EntitySerializer {

    private static final Path path = Path.of("src/main/resources/world/world.dat");

    public static void serialize() {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path.toFile()))) {
            writeEntities(out);
            System.out.println(out.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeEntities(DataOutputStream out) throws IOException {
        var entities = EntityManager.getComponents(SerializableComponent.class).keySet();

        for (Entity entity : entities) {
            var cEntity = (SerializableEntity) entity;
            out.writeInt(cEntity.getType());
            cEntity.serialize(out);
            EntityManager.removeEntitySafe(entity);
        }
    }

    public static void deserialize() {
        try (DataInputStream in = new DataInputStream(new FileInputStream(path.toFile()))) {
            System.out.println(in.available());
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
            default: throw new IllegalArgumentException("Bad entity type: " + type);
        }
    }

}
