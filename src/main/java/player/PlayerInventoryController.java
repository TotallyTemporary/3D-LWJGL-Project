package player;

import animation.Animation;
import animation.AnimatorComponent;
import animation.KeyFrame;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import item.*;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import render.Player;
import ui.UIModelComponent;

import java.lang.Math;
import java.util.ArrayList;

public class PlayerInventoryController extends Component {

    private static final int STACKS_COUNT = 9;
    private static final float STACK_TEXTURE_SCALE = 0.07f,
                               STACK_SEPARATION = 0.08f,
                               HOTBAR_POSITION_Y = -0.85f; // relative from center of screen
    private static float HAND_ITEM_SCALE = 1 / 3f;

    private static Vector3f HAND_POSITION_OFFSET = new Vector3f(0.45f, -0.45f, -0.80f);

    private static Quaternionf HAND_ROTATION_OFFSET =
            new Quaternionf().identity()
                    .rotateY((float) Math.toRadians(180 + 102));
    // create block place hand animation
    private static final Animation handPlaceAnimation;
    static {
        var keyFrames = new ArrayList<KeyFrame>();
        keyFrames.add(new KeyFrame(0f, new Matrix4f().identity()));
        keyFrames.add(new KeyFrame(0.5f,
            new Matrix4f().translation(-1.75f, 0f, 0f)
                    .rotateZ(45f)
        ));
        keyFrames.add(new KeyFrame(1f, new Matrix4f().identity()));

        handPlaceAnimation = new Animation(keyFrames, Animation.PlaybackMode.PLAY_ONCE);
    }

    private static final long HAND_PLACE_ANIMATION_LENGTH = 200; // millis

    private static final long HAND_BOB_ANIMATION_LENGTH = 500;
    private static final Animation handBobAnimation;
    static {
        var keyFrames = new ArrayList<KeyFrame>();
        keyFrames.add(new KeyFrame(0f, new Matrix4f().identity()));
        keyFrames.add(new KeyFrame(0.5f, new Matrix4f().translation(-0.05f, 0.15f, 0)));
        keyFrames.add(new KeyFrame(1f, new Matrix4f().identity()));

        handBobAnimation = new Animation(keyFrames, Animation.PlaybackMode.LOOP_FOREVER);
        handBobAnimation.start(HAND_BOB_ANIMATION_LENGTH);
    }

    private static final long HAND_BREAK_ANIMATION_LENGTH = 250;
    private static final Animation handBreakAnimation;
    static {
        var keyFrames = new ArrayList<KeyFrame>();
        keyFrames.add(new KeyFrame(0f, new Matrix4f().identity()));
        keyFrames.add(new KeyFrame(0.5f, new Matrix4f()
                .rotateZ(45f)));
        keyFrames.add(new KeyFrame(1f, new Matrix4f().identity()));

        handBreakAnimation = new Animation(keyFrames, Animation.PlaybackMode.LOOP_FOREVER);
    }
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

        // update the hand bob animation
        // TODO all this hand and animation crap is not related to the inventory at all
        // TODO separate into own PlayerHandComponent or something
        {
            var movementController = EntityManager.getComponent(entity, PlayerMovementController.class);
            if (movementController.isPlayerMoving() && handBobAnimation.hasBeenPaused()) {
                handBobAnimation.unpause();
            }

            if (!movementController.isPlayerMoving() && !handBobAnimation.hasBeenPaused()) {
                handBobAnimation.pause();
            }
        }

        // update hand break animation
        {
            var breakController = EntityManager.getComponent(entity, PlayerBlockController.class);
            if (breakController.isBreakingBlock() && handBreakAnimation.hasEnded()) {
                handBreakAnimation.start(HAND_BREAK_ANIMATION_LENGTH);
            }

            if (!breakController.isBreakingBlock() && !handBreakAnimation.hasEnded()) {
                handBreakAnimation.stop();
            }
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
        } else {
            // if this wasnt our last item, we animate the place hand animation
            handPlaceAnimation.start(HAND_PLACE_ANIMATION_LENGTH);
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
        EntityManager.addComponent(item, new PlayerHandItemModelComponent(itemSpec.getModel()));
        var animator = new AnimatorComponent();
        animator.attachAnimation(handPlaceAnimation);
        animator.attachAnimation(handBobAnimation);
        animator.attachAnimation(handBreakAnimation);
        EntityManager.addComponent(item, animator);

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
