package item;

import entity.Entity;
import entity.ModelComponent;
import entity.SerializableComponent;

public class ItemModelComponent extends ModelComponent {

    public ItemModelComponent(int itemID) {
        super(ItemType.getByID(itemID).getModel());
    }

    @Override
    public void destroy(Entity entity) {
        // don't destroy this model!
    }
}
