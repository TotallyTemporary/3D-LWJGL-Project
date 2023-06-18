package structures;

import biome.Structure;
import chunk.Block;
import chunk.Chunk;
import org.joml.Vector3i;

import java.util.function.Supplier;

public class Tree extends Structure {
    public void make(Chunk chunk, Vector3i position, Supplier<Float> roll) {
        int x = position.x;
        int y = position.y;
        int z = position.z;

        int treeTrunkLength = 3;

        // make trunk
        for (int dy = 1; dy < treeTrunkLength; dy++) {
            chunk.setBlockSafe(x, y+dy, z, Block.OAK_LOG.getID());
        }

        // make leaves
        for (int dx = -1; dx <= 1; dx++)
        for (int dy = -1; dy <= 1; dy++)
        for (int dz = -1; dz <= 1; dz++)
        {
            chunk.setBlockSafe(x+dx, y+dy+treeTrunkLength, z+dz, Block.OAK_LEAVES.getID());
        }
    }
}
