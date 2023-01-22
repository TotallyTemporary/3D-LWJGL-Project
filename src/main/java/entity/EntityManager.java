package entity;

import item.ItemComponent;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class should be used statically. It handles relationships between entities and components.
 * Entities are just a wrapper for a UUID.
 */
public class EntityManager {
    private static HashMap<Class<? extends Component>, HashMap<Entity, Component>> map = new HashMap<>();
    private static ArrayList<Map.Entry<Entity, Component>> toBeRemoved = new ArrayList<>();

    /** This updates the state of the EntityManager.
     * it removes components that have been marked as toBeRemoved. */
    public static synchronized void update() {
        // remove components that must be removed.
        var it = toBeRemoved.iterator();
        while (it.hasNext()) {
            var entry = it.next();
            var classMap = map.get(entry.getValue());
            if (classMap != null) {
                classMap.remove(entry.getKey());
            }
            it.remove();
        }
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

    public static synchronized void removeComponentSafe(Entity entity, Class<? extends Component> clazz) {
        toBeRemoved.add(new AbstractMap.SimpleEntry(entity, clazz));
    }

    /** Attempts to remove a component from an entity. NOTE: Do not call from entity update. */
    public static synchronized <T extends Component> T removeComponent(Entity entity, Class<T> clazz) {
        var classMap = map.get(clazz);
        if (classMap == null) return null;
        return (T) classMap.remove(entity);
    }

    /** Attemps to remove all components associated with an entity. NOTE: Do not call from entity update. */
    public static synchronized void removeEntity(Entity entity) {
        map.forEach((_x, value) -> value.remove(entity));
    }

    public static synchronized void removeEntitySafe(Entity entity) {
        map.keySet().forEach(clazz -> removeComponentSafe(entity, clazz));
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
