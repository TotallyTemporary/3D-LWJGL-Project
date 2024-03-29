package entity;

public abstract class Component {
    public abstract void apply(Entity entity);
    public abstract void destroy(Entity entity);
}