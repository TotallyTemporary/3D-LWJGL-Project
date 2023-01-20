package entity;

public abstract class Component {
    public abstract void start(Entity entity);
    public abstract void  stop(Entity entity);
    public abstract void apply(Entity entity);
}