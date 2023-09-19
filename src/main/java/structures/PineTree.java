package structures;

import biome.Structure;
import block.Block;
import chunk.Chunk;
import org.joml.Vector3i;

import java.util.function.Supplier;

public class PineTree extends Structure {

    private static final float STEEPNESS = 1.7f;

    public void make(Chunk chunk, Vector3i position, Supplier<Float> roll) {
        int treeTrunkLength = (int) (7 + roll.get() * 3); // 7-9 blocks
        int groundClearance = 2; // (int) (2 + roll.get()); // 1 - 2 blocks

        if (chunk.getBlockSafe(position.x, position.y + 1, position.z) != Block.AIR.getID()) {
            return;
        }

        makeLeaves(chunk, position.x, position.y+1, position.z, treeTrunkLength, groundClearance);
        makeTrunk(chunk, position.x, position.y+1, position.z, treeTrunkLength);
    }

    private void makeTrunk(Chunk chunk, int x, int y, int z, int trunkLength) {
        for (int dy = 0; dy < trunkLength; dy++) {
            chunk.setBlockSafe(x, y+dy, z, Block.PINE_LOG.getID());
        }
    }

    private void makeLeaves(Chunk chunk, int x, int y, int z,
                            int trunkLength, int groundClearance) {
        int leavesSize = trunkLength - groundClearance;

        for (int i = 0; i <= leavesSize; i++) {
            float radius = (i + 1) / STEEPNESS;
            int maxRadius = (int) (radius + 1);
            int dy = trunkLength - i;
            // iterate over a square and check if radius is fine
            for (int dx = -maxRadius; dx <= maxRadius; dx++)
            for (int dz = -maxRadius; dz <= maxRadius; dz++)
            {
                if (dx*dx + dz*dz < radius*radius) {
                    // System.out.println(dx + ", " + dy + ", " + dz);
                    chunk.setBlockSafe(x+dx, y+dy, z+dz, Block.PINE_LEAVES.getID());
                }
            }
        }
    }

}
