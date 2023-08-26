package player;

import block.Block;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import item.ItemStack;
import item.ItemThumbnailRenderer;
import item.ItemThumbnailTexture;
import org.joml.Vector3f;
import render.Texture;
import ui.UIModelComponent;

public class PlayerInventoryController extends Component {

    private static final int STACKS_COUNT = 9;
    private static final float STACK_TEXTURE_SCALE = 0.07f,
                               STACK_SEPARATION = 0.08f,
                               HOTBAR_POSITION_Y = -0.85f; // relative from center of screen

    private ItemStack[] stacks = new ItemStack[STACKS_COUNT];
    private Entity[] stackEntities = new Entity[STACKS_COUNT];

    @Override
    public void apply(Entity entity) {

    }

    public boolean addItem(int itemID) {
        /** Tries to add an item to our hotbar. Returns true if successful, false if no space. */
        for (int i = 0; i < STACKS_COUNT; i++) {
            if (stacks[i] == null) {
                stacks[i] = new ItemStack(itemID);
                stackEntities[i] = createStackEntity(itemID, i);
            }
            if (stacks[i].getItemID() == itemID) {
                stacks[i].incrementCount();
                return true;
            }
        }
        return false;
    }

    private Entity createStackEntity(int itemID, int hotbarIndex) {
        ItemThumbnailTexture texture = ItemThumbnailRenderer.renderItem(itemID);

        // create a ui entity for hotbar texture thing
        var textureModelEntity = new Entity();
        EntityManager.addComponent(textureModelEntity, new UIModelComponent(texture));

        // calculate position on screen
        int middleIndex = STACKS_COUNT / 2; // TODO doesn't work with even stack sizes
        int indexDelta = hotbarIndex - middleIndex;
        float dx = indexDelta * STACK_SEPARATION;

        var transformation = new TransformationComponent(
            new Vector3f(dx, HOTBAR_POSITION_Y, 0),
            new Vector3f(),
            new Vector3f(STACK_TEXTURE_SCALE, STACK_TEXTURE_SCALE, STACK_TEXTURE_SCALE)
        );
        EntityManager.addComponent(textureModelEntity, transformation);

        return textureModelEntity;
    }
}
