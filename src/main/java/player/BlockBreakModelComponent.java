package player;

import block.Block;
import entity.ModelComponent;

public class BlockBreakModelComponent extends ModelComponent {

    private Block block;
    private int index;

    public BlockBreakModelComponent() {
        super(Block.INVALID.getBreakModel());
        this.block = Block.INVALID;
    }

    public void setBlock(Block block) {
        this.block = block;
        super.model = block.getBreakModel();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public Block getBlock() {
        return block;
    }
}
