package structures;

import biome.Structure;
import block.Block;
import chunk.Chunk;
import org.joml.Vector3i;

import java.util.function.Supplier;

public class WildWheat extends Structure {

    public void make(Chunk chunk, Vector3i position, Supplier<Float> roll) {
        if (chunk.getBlockSafe(position.x, position.y + 1, position.z) != Block.AIR.getID()) {
            return;
        }

        int growthStage = (int) (roll.get() * 8);
        byte block = (byte) (Block.WHEAT.getID() + growthStage);

        chunk.setBlockSafe(position.x, position.y + 1, position.z, block);
    }

}
