package player;

import block.Block;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class BlockBreak extends Entity {

    private static final int TEX_INDEX = 240;
    private static final int TEX_COUNT = 10;

    private static final float MODEL_SIZE = 1.03f;
    private static final float MODEL_ROTATION = (float) Math.toRadians(1);

    private TransformationComponent transformationComponent;
    private BlockBreakModelComponent modelComponent;

    public BlockBreak() {
        super();

        modelComponent = new BlockBreakModelComponent();
        transformationComponent = new TransformationComponent(
                new Vector3f(),
                new Vector3f(MODEL_ROTATION),
                new Vector3f(MODEL_SIZE)
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

    public void setBreakage(float percentage) {
        modelComponent.setIndex(TEX_INDEX + (int) (Math.min(percentage, 0.99f) * TEX_COUNT));
    }

}
