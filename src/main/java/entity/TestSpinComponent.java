package entity;

import main.Timer;

public class TestSpinComponent extends Component {
    final float SPEED = 1/3_000f;

    @Override public void start() {}
    @Override public void stop() {}
    @Override
    public void apply(Entity entity) {
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        var rot = transform.getRotation();
        rot.x += SPEED * Timer.getFrametimeMillis();
        rot.y += SPEED * Timer.getFrametimeMillis();
        rot.z += SPEED * Timer.getFrametimeMillis();
    }
}
