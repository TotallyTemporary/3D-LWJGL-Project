package structures;

import biome.Structure;
import chunk.Block;
import chunk.Chunk;
import org.joml.Vector3i;

import java.util.function.Supplier;

public class Cactus extends Structure {

    public void make(Chunk chunk, Vector3i position, Supplier<Float> roll) {
        int length = (int) (2 + roll.get() * 2); // 2, 3, or 4

        byte cactus = Block.CACTUS.getID();
        for (int dy = 1; dy < length+1; dy++) {
            chunk.setBlockSafe(position.x, position.y + dy, position.z, cactus);
        }
    }
}
