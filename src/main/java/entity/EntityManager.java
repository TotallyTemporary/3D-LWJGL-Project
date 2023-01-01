package entity;

import java.util.HashMap;

/**
 * This class should be used statically. It handles relationships between entities and components.
 * Entities are just a wrapper for a UUID.
 */
public class EntityManager {
    private static HashMap<Class<? extends Component>, HashMap<Entity, Component>> map = new HashMap<>();
    private static HashMap<Class<? extends Component>, Boolean> updatedThisFrame = new HashMap<>();

    /**
     * Calls start() on all components and resets the map of already updated classes.
     */
    public static void start() {
        // nothing has been updated yet.
        for (var clazz : map.keySet()) {
            updatedThisFrame.put(clazz, false);
        }
        // start all comps
        map.forEach((clazz, classMap) -> { classMap.forEach((entity, component) -> component.start()); });
    }

    /**
     * Calls stop() for all components, and also calls apply() on all components not yet updated.
     */
    public static void stop() {
        for (var clazz : map.keySet()) {
            if (!updatedThisFrame.get(clazz)) {
                updateComponents(clazz);
            }
        }
        // stop all comps
        map.forEach((clazz, classMap) -> { classMap.forEach((entity, component) -> component.stop()); });
    }

    public static void addComponent(Entity entity, Component component) {
        var clazz = component.getClass();
        var classMap = map.get(clazz);
        if (classMap == null) {
            classMap = new HashMap<>();
            map.put(clazz, classMap);
        }

        classMap.put(entity, component);
    }

    /** Attempts to remove a component from an entity. NOTE: Do not call from entity update. */
    public static boolean removeComponent(Entity entity, Component component) {
        var clazz = component.getClass();
        var classMap = map.get(clazz);
        if (classMap == null) return false;
        return classMap.remove(entity, component);
    }

    /** Attemps to remove all components associated with an entity. NOTE: Do not call from entity update. */
    public static void removeEntity(Entity entity) {
        map.entrySet().forEach(entry -> entry.getValue().remove(entity));
    }

    public static boolean hasComponent(Entity entity, Class<? extends Component> clazz) {
        var classMap = map.get(clazz);
        if (classMap == null) return false;
        return classMap.containsKey(entity);
    }

    public static <T extends Component> T getComponent(Entity entity, Class<T> clazz) {
        var classMap = map.get(clazz);
        if (classMap == null) return null;
        return (T) classMap.get(entity);
    }

    public static <T extends Component> HashMap<Entity, T> getComponents(Class<T> clazz) {
        return (HashMap<Entity, T>) map.get(clazz);
    }

    public static void updateComponents(Class<? extends Component> clazz) {
        updatedThisFrame.put(clazz, true);
        var classMap = map.get(clazz);
        if (classMap == null) return;
        classMap.forEach((entity, component) -> component.apply(entity));
    }
}
