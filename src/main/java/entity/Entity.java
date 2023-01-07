package entity;

import java.util.Random;
import java.util.UUID;

/** An entity is something that can have components attached to it.
 * @see Component
 * @see EntityManager
 * */
public class Entity {
    private UUID uuid;

    static Random r = new Random();
    static { r.setSeed(System.nanoTime()); }
    public Entity() {
        this.uuid = new UUID(r.nextLong(), r.nextLong());
    }

    public Entity(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return this.uuid.equals(other);
    }

}
