package structures;

import biome.Structure;
import block.Block;
import chunk.Chunk;
import org.joml.Vector3i;

import java.util.function.Supplier;

public class WildPotato extends Structure {

    public void make(Chunk chunk, Vector3i position, Supplier<Float> roll) {
        int growthStage = (int) (roll.get() * 4);
        byte block = (byte) (Block.POTATO.getID() + growthStage);

        if (chunk.getBlockSafe(position.x, position.y + 1, position.z) != Block.AIR.getID()) {
            return;
        }

        chunk.setBlockSafe(position.x, position.y + 1, position.z, block);
    }

}
