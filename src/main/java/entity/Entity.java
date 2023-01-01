package entity;

import java.util.UUID;

/** An entity is something that can have components attached to it.
 * @see Component
 * @see EntityManager
 * */
public class Entity {
    private UUID uuid;

    public Entity() {
        this.uuid = UUID.randomUUID();
    }

    public Entity(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

}
