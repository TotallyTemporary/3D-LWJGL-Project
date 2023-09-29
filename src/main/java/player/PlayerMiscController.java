package player;

import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import item.ItemComponent;
import org.joml.Vector3f;

// TODO This class contains miscellaneous bits and bobs of player functionality.
public class PlayerMiscController extends Component {

    private static final float ITEM_PICKUP_RANGE    = 0.5f,
                               ITEM_ATTRACT_RANGE   = 3.5f,
                               ATTRACT_ACCELERATION = 40f;

    @Override
    public void apply(Entity entity) {
        var pos = EntityManager.getComponent(entity, TransformationComponent.class).getPosition();

        var res = new Vector3f(); // reuseable vector object
        var comps = EntityManager.getComponents(ItemComponent.class).entrySet();

        for (var entry : comps) {
            var item = entry.getKey();
            var itemID = entry.getValue().getItemID();
            var itemPos = EntityManager.getComponent(item, TransformationComponent.class).getPosition();
            var physics = EntityManager.getComponent(item, PhysicsObjectComponent.class);

            itemPos.sub(pos, res);
            if (res.length() < ITEM_PICKUP_RANGE) {
                var inventory = EntityManager.getComponent(entity, PlayerInventoryController.class);
                if (inventory == null) {
                    continue; // player has no inventory
                }

                boolean couldPickUp = inventory.addItem(itemID);
                if (couldPickUp) {
                    EntityManager.removeEntitySafe(item);
                }
            }
            else if (res.length() < ITEM_ATTRACT_RANGE) {
                var moveDir = res.normalize().mul(-ATTRACT_ACCELERATION);
                physics.acceleration.add(moveDir);
            }
        }
    }

    @Override
    public void destroy(Entity entity) {}
}
