package item;

public class ItemStack {

    private int itemID;
    private int itemCount;

    public ItemStack(int itemID) {
        this.itemID = itemID;
        this.itemCount = 0;
    }

    public int getItemID() {
        return itemID;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void incrementCount() {
        itemCount += 1;
    }

    public void decrementCount() {
        itemCount -= 1;
    }

}
