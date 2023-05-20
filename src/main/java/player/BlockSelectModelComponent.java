package player;

import chunk.Block;
import entity.ModelComponent;

public class BlockSelectModelComponent extends ModelComponent {

    private Block block;

    public BlockSelectModelComponent() {
        super(Block.INVALID.getSelectModel());
        this.block = Block.INVALID;
    }

    public void setBlock(Block block) {
        this.block = block;
        super.model = block.getSelectModel();
    }

    public Block getBlock() {
        return block;
    }
}
