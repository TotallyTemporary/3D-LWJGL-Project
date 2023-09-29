package entity;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class should be used statically. It handles relationships between entities and components.
 * Entities are just a wrapper for a UUID.
 */
public class EntityManager {
    private static HashMap<Class<? extends Component>, HashMap<Entity, Component>> map = new HashMap<>();
    private static ConcurrentLinkedQueue<Map.Entry<Entity, Class<? extends Component>>> toBeRemovedComponents = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<Entity> toBeRemovedEntities = new ConcurrentLinkedQueue<>();

    /** This updates the state of the EntityManager.
     * it removes components that have been marked as toBeRemoved. */
    public static synchronized void update() {
        // remove components that should get removed
        var it = toBeRemovedComponents.iterator();
        while (it.hasNext()) {
            var entry = it.next();
            Entity entity = entry.getKey();
            var classMap = map.get(entry.getValue());
            if (classMap != null) {
                if (classMap.containsKey(entity)) {
                    classMap.get(entity).destroy(entity);
                    classMap.remove(entity);
                }
            }
        }
        toBeRemovedComponents.clear();

        // remove entities that should get removed
        var jt = toBeRemovedEntities.iterator();
        while (jt.hasNext()) {
            Entity entity = jt.next();

            var kt = map.values().iterator();
            while (kt.hasNext()) {
                var classMap = kt.next();
                classMap.remove(entity);
            }
        }

        toBeRemovedEntities.clear();
    }

    public static synchronized void addComponent(Entity entity, Component component) {
        var clazz = component.getClass();
        var classMap = map.get(clazz);
        if (classMap == null) {
            classMap = new HashMap<>();
            map.put(clazz, classMap);
        }

        classMap.put(entity, component);
    }

    public static void removeComponentSafe(Entity entity, Class<? extends Component> clazz) {
        toBeRemovedComponents.add(new AbstractMap.SimpleEntry(entity, clazz));
    }

    /** Attempts to remove a component from an entity. NOTE: Do not call from entity update. */
    public static synchronized <T extends Component> T removeComponent(Entity entity, Class<T> clazz) {
        var classMap = map.get(clazz);
        if (classMap == null) return null;
        if (classMap.containsKey(entity)) {
            classMap.get(entity).destroy(entity);
        }

        return (T) classMap.remove(entity);
    }

    /** Attemps to remove all components associated with an entity. NOTE: Do not call from entity update. */
    public static synchronized void removeEntity(Entity entity) {
        map.forEach((component, classMap) -> {
            if (classMap.containsKey(entity)) {
                classMap.get(entity).destroy(entity);
                classMap.remove(entity);
            }
        });
    }

    public static void removeEntitySafe(Entity entity) {
        toBeRemovedEntities.add(entity);
    }

    public static synchronized boolean hasComponent(Entity entity, Class<? extends Component> clazz) {
        var classMap = map.get(clazz);
        if (classMap == null) return false;
        return classMap.containsKey(entity);
    }

    public static synchronized <T extends Component> T getComponent(Entity entity, Class<T> clazz) {
        var classMap = map.get(clazz);
        if (classMap == null) return null;
        return (T) classMap.get(entity);
    }

    public static synchronized <T extends Component> HashMap<Entity, T> getComponents(Class<T> clazz) {
        var classMap = (HashMap<Entity, T>) map.get(clazz);
        if (classMap == null) {
            classMap = new HashMap<>();
        }
        return classMap;
    }

    public static synchronized void updateComponents(Class<? extends Component> clazz) {
        var classMap = map.get(clazz);
        if (classMap == null) return;
        for (var entry : classMap.entrySet()) {
            entry.getValue().apply(entry.getKey());
        }
    }
}
