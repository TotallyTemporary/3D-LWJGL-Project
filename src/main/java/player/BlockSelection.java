package player;

import block.Block;
import block.CardinalDirection;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import org.joml.Vector3f;
import org.joml.Vector3i;
import render.Texture;
import shader.Shader;

public class BlockSelection {

    public static final float OUTLINE_SCALE = 1.02f;
    public static final float NORMAL_SCALE = 1f;
    public static final float OUTLINE_DARKNESS = 0.45f;
    public static final float NORMAL_DARKNESS = 0.80f; // unused since selection block has its colour mask turned to false

    private static Shader selectionShader = null;
    private static Texture blockTexture = null;

    public static void setShader(Shader shader) {
        selectionShader = shader;
    }

    public static void setTexture(Texture texture) {
        blockTexture = texture;
    }

    private SelectedFaceModelComponent[] modelComponents = new SelectedFaceModelComponent[CardinalDirection.COUNT];
    private TransformationComponent[] transformationComponents = new TransformationComponent[CardinalDirection.COUNT];
    private Entity[] faceEntities = new Entity[CardinalDirection.COUNT];

    public BlockSelection() {
        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            faceEntities[i] = new Entity();

            modelComponents[i] = new SelectedFaceModelComponent(blockTexture, selectionShader);
            transformationComponents[i] = new TransformationComponent();

            EntityManager.addComponent(faceEntities[i], modelComponents[i]);
            EntityManager.addComponent(faceEntities[i], transformationComponents[i]);
        }

    }

    public void setBlock(Block block) {
        if (block == null) {
            setBlock(Block.INVALID);
            return;
        }

        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            modelComponents[i].setFace(block.getFace(i));
        }
    }

    public void setLocation(Vector3i loc) {
        if (loc == null) {
            return;
        }

        for (int i = 0; i < CardinalDirection.COUNT; i++) {
            transformationComponents[i]
                    .getPosition()
                    .set(new Vector3f(loc.x + 0.5f, loc.y + 0.5f, loc.z + 0.5f));
            transformationComponents[i].forceRecalculate();
        }
    }
}
