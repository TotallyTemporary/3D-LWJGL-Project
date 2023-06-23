package structures;

import biome.Structure;
import block.Block;
import chunk.Chunk;
import org.joml.Vector3i;

import java.util.function.Supplier;

public class Flower extends Structure {
    public void make(Chunk chunk, Vector3i position, Supplier<Float> roll) {
        if (chunk.getBlockSafe(position.x, position.y + 1, position.z) != Block.AIR.getID()) {
            return;
        }

        byte flower;
        if (roll.get() < 0.5f) {
            flower = Block.DANDELION.getID();
        } else {
            flower = Block.ROSE.getID();
        }

        chunk.setBlockSafe(position.x, position.y + 1, position.z, flower);
    }
}
