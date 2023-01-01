package entity;

public abstract class Component {
    public abstract void start();
    public abstract void stop();
    public abstract void apply(Entity entity);
}