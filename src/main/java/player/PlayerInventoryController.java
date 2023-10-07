package player;

import block.Block;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import item.*;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import render.Player;
import render.Texture;
import ui.UIModelComponent;

import java.lang.Math;

public class PlayerInventoryController extends Component {

    private static final int STACKS_COUNT = 9;
    private static final float STACK_TEXTURE_SCALE = 0.07f,
                               STACK_SEPARATION = 0.08f,
                               HOTBAR_POSITION_Y = -0.85f; // relative from center of screen
    private static float HAND_ITEM_SCALE = 1 / 3f;

    private static Vector3f HAND_POSITION_OFFSET = new Vector3f(0.45f, -0.45f, -0.80f);

    private static Quaternionf HAND_ROTATION_OFFSET =
            new Quaternionf().identity()
                    .rotateY((float) Math.toRadians(102))
                    .rotateX((float) Math.toRadians(0));
    private static int selectedHotbarSlot = 0;
    private static Entity selectedHotbarItem = null;

    public PlayerInventoryController() {
        setHandItem();
    }

    private ItemStack[] stacks = new ItemStack[STACKS_COUNT];
    private Entity[] stackEntities = new Entity[STACKS_COUNT];

    @Override
    public void apply(Entity entity) {
        // update hotbar selected slot
        int newSlot = getPossibleSelectedStack();
        if (newSlot != -1 && newSlot != selectedHotbarSlot) {
            selectedHotbarSlot = newSlot;
            setHandItem();
        }

        // update hand position to be in front of the camera
        if (selectedHotbarItem != null) {
            Player camera = (Player) entity;
            setHandItemPosition(selectedHotbarItem, camera);
        }
    }

    @Override
    public void destroy(Entity entity) {}

    public boolean addItem(int itemID) {
        /** Tries to add an item to our hotbar. Returns true if successful, false if no space. */
        for (int i = 0; i < STACKS_COUNT; i++) {
            if (stacks[i] == null) {
                // create a new itemstack for this item
                stacks[i] = new ItemStack(itemID);
                stackEntities[i] = createStackEntity(itemID, i);

                // if this itemstack is selected, we need to update the hand model
                if (i == selectedHotbarSlot) {
                    setHandItem();
                }
            }
            if (stacks[i].getItemID() == itemID) {
                stacks[i].incrementCount();
                return true;
            }
        }
        return false;
    }

    /** Tries to remove an item from our selected hotbar slot */
    public void removeItem() {
        ItemStack stack = stacks[selectedHotbarSlot];
        if (stack == null) {
            return;
        }

        stack.decrementCount();

        if (stack.getItemCount() == 0) {
            stacks[selectedHotbarSlot] = null;
            setHandItem(); // remove hand item

            // remove hotbar icon
            Entity hotbarItem = stackEntities[selectedHotbarSlot];
            EntityManager.removeEntitySafe(hotbarItem);
            stackEntities[selectedHotbarSlot] = null;
        }
    }

    public ItemType getSelectedItem() {
        ItemStack stack = stacks[selectedHotbarSlot];
        if (stack == null) {
            return ItemType.INVALID;
        }
        int itemID = stack.getItemID();
        return ItemType.getByID(itemID);
    }

    /** Returns 0-8 for numbers 1-9. Returns -1 for no key down. */
    private int getPossibleSelectedStack() {
        for (int index = 0; index < 9; index++) {
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_1 + index)) {
                return index;
            }
        }
        return -1;
    }

    private void setHandItemPosition(Entity item, Player camera) {
        Matrix4f inverseViewMatrix = camera.getViewMatrix().invert();
        var transform = EntityManager.getComponent(item, TransformationComponent.class);

        Vector4f viewFront = new Vector4f(HAND_POSITION_OFFSET, 0).mul(inverseViewMatrix);
        Vector3f cameraFront = new Vector3f(viewFront.x, viewFront.y, viewFront.z);
        cameraFront.normalize();

        // calculate new position
        Vector3f cameraPosition = camera.getEyePosition();
        Vector3f itemPosition = cameraPosition.add(cameraFront);
        transform.setPosition(itemPosition);

        // calculate new rotation
        Quaternionf rotation = inverseViewMatrix.getNormalizedRotation(new Quaternionf());
        rotation.mul(HAND_ROTATION_OFFSET);
        Vector3f eulerAngles = rotation.getEulerAnglesXYZ(new Vector3f());
        transform.setRotation(eulerAngles);
    }

    private void setHandItem() {
        if (selectedHotbarItem != null) {
            EntityManager.removeEntitySafe(selectedHotbarItem);
        }

        if (stacks[selectedHotbarSlot] == null) {
            // no item in this slot
            selectedHotbarItem = null;
            return;
        }

        var itemID = stacks[selectedHotbarSlot].getItemID();
        var item = new Entity();

        EntityManager.addComponent(item, new TransformationComponent(
            new Vector3f(),
            new Vector3f(),
            new Vector3f(HAND_ITEM_SCALE)
        ));

        var itemSpec = ItemType.getByID(itemID);
        EntityManager.addComponent(item, new ItemModelComponent(itemSpec.getModel()));

        selectedHotbarItem = item;
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
