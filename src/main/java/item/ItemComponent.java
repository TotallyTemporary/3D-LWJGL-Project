package item;

import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import main.Timer;
import org.joml.Vector3f;

public class ItemComponent extends Component {

    // TODO bobbing was removed because it updated the transformationcomponent of the item, which is used by the PhysicsObjectComponent
    // TODO to apply physics onto the item.

    /*
    // bobbing
    private Vector3f actualPosition, actualRotation;
    private float positionTimer = 0,
                  rotationTimer = 0;

    // bob variables
    private static final float  MIN_HOVER_HEIGHT = 0.25f,
                                HOVER_AMPLITUDE  = 0.10f,
                                POSITION_SPEED = 2f,
                                ROTATION_SPEED = 2f;

     */

    private int itemID;

    public ItemComponent(int itemID /*Vector3f actualPosition, Vector3f actualRotation*/) {
        /*this.actualPosition = actualPosition;
        this.actualRotation = actualRotation;*/
        this.itemID = itemID;
    }

    public int getItemID() {
        return itemID;
    }

    @Override
    public void apply(Entity entity) {
        /*var transform = EntityManager.getComponent(entity, TransformationComponent.class);

        positionTimer = (float) ((positionTimer + Timer.getFrametimeSeconds() * POSITION_SPEED) % (2*Math.PI));
        rotationTimer = (float) ((rotationTimer + Timer.getFrametimeSeconds() * ROTATION_SPEED) % (2*Math.PI));

        transform.setPosition(new Vector3f(
                actualPosition.x,
                actualPosition.y + MIN_HOVER_HEIGHT + (float) Math.sin(positionTimer)*HOVER_AMPLITUDE,
                actualPosition.z
        ));

        transform.setRotation(new Vector3f(
                actualRotation.x,
                actualRotation.y + rotationTimer,
                0
        ));*/
    }

    /*
    public Vector3f getActualPosition() {
        return actualPosition;
    }

    public Vector3f getActualRotation() {
        return actualRotation;
    }
     */

    @Override public void destroy(Entity entity) {}
}
