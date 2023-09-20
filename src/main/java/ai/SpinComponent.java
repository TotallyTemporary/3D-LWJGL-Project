package ai;

import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import main.Timer;
import org.joml.Vector3f;

public class SpinComponent extends Component {

    private Vector3f speed;

    public SpinComponent(Vector3f speed) {
        this.speed = speed;
    }

    public SpinComponent() {
        this(new Vector3f(0, -2, 0));
    }

    @Override
    public void apply(Entity entity) {
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        Vector3f rotation = transform.getRotation();
        Vector3f delta = speed.mul(Timer.getFrametimeSeconds(), new Vector3f());
        transform.setRotation(delta.add(rotation));
    }
}
