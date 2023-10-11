package block;

import entities.EntityType;
import entity.EntityManager;
import item.ItemType;
import org.joml.Vector3i;
import player.PlayerInventoryController;
import render.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CraftingTableBlockEntity extends BlockEntity {

    // TODO currently just counts how many times we clicked the crafting table
    private int timesClicked = 0;

    public CraftingTableBlockEntity(Vector3i location) {
        super(location);
    }

    public CraftingTableBlockEntity() {
        super(); // only called when deserializing
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
        timesClicked += 1;
        System.out.println("Clicked " + timesClicked + " times!");
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(timesClicked);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        super.deserialize(in);
        timesClicked = in.readInt();
    }

    @Override
    public int getType() {
        return EntityType.CRAFTING_TABLE;
    }
}
