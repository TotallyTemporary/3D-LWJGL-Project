package block;

import entity.EntityManager;
import item.ItemType;
import org.joml.Vector3i;
import player.PlayerInventoryController;
import render.Player;

public class CraftingTableBlockEntity extends BlockEntity {
    public CraftingTableBlockEntity(Vector3i location, Block block) {
        super(location, block);
    }

    @Override
    public void onBreakBlock() {

    }

    @Override
    public void onPlaceBlock() {

    }

    @Override
    public void onInteractBlock(Player player) {
        var inventory = EntityManager.getComponent(player, PlayerInventoryController.class);
        inventory.addItem(ItemType.IRON_PICKAXE.getID());
    }
}
