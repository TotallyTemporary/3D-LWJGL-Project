package animation;

import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Matrix4f;

public class AnimatorComponent extends Component {

    private Animation animation;

    public AnimatorComponent(Animation animation) {
        this.animation = animation;
    }

    @Override
    public void apply(Entity entity) {
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        Matrix4f animationTransform = animation.getTransformation();
        transform.doTransformation(animationTransform);
    }

    @Override
    public void destroy(Entity entity) {
        // TODO maybe call animation.stop() here
        // although we didnt call .start() it might be convenient
    }

    public Animation getAnimation() {
        return animation;
    }
}
