package player;

import chunk.Block;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class BlockSelection extends Entity {

    public static final float OUTLINE_SCALE = 1.04f;
    public static final float NORMAL_SCALE = 1.01f;
    public static final float OUTLINE_DARKNESS = 0.45f;
    public static final float NORMAL_DARKNESS = 0.80f;

    private TransformationComponent transformationComponent;
    private BlockSelectModelComponent modelComponent;

    public BlockSelection() {
        super();

        modelComponent = new BlockSelectModelComponent();
        transformationComponent = new TransformationComponent(
                new Vector3f(),
                new Vector3f(),
                new Vector3f(1f)
        );

        EntityManager.addComponent(this, modelComponent);
        EntityManager.addComponent(this, transformationComponent);
    }

    public void setBlock(Block block) {
        if (block == null) {
            setBlock(Block.INVALID);
        } else {
            modelComponent.setBlock(block);
        }
    }

    public void setLocation(Vector3i loc) {
        if (loc == null) {
            return;
        }
        transformationComponent.getPosition().set(loc.x + 0.5f, loc.y + 0.5f, loc.z + 0.5f);
    }
}
