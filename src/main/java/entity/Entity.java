package entity;

import java.util.UUID;

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
