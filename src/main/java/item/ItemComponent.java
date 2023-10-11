package item;

import entity.*;

public class ItemComponent extends Component {

    private int itemID;

    public ItemComponent(int itemID) {
        this.itemID = itemID;
    }

    public int getItemID() {
        return itemID;
    }

    @Override
    public void apply(Entity entity) {

    }

    @Override public void destroy(Entity entity) {}
}
