package animation;

import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class AnimatorComponent extends Component {

    private List<Animation> animations = new ArrayList<>();

    public void attachAnimation(Animation animation) {
        animations.add(animation);
    }

    @Override
    public void apply(Entity entity) {
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        for (Animation animation : animations) {
            Matrix4f animationTransform = animation.getTransformation();
            transform.doTransformation(animationTransform);
        }
    }

    @Override
    public void destroy(Entity entity) {
    }

}
