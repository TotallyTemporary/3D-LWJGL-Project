package block;

import java.util.Objects;

/** This class is for block faces that are in the world.
 * For example, two grass block faces aren't the same, if one is in the shadow, and the other isn't. */
public class WorldBlockFace {

    private BlockFace blockFaceDefinition;
    private byte light;

    public WorldBlockFace(BlockFace blockFaceDefinition, byte light) {
        this.blockFaceDefinition = blockFaceDefinition;
        this.light = light;
    }

    public BlockFace getBlockFaceDefinition() {
        return blockFaceDefinition;
    }

    public byte getLight() {
        return light;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorldBlockFace)) return false;
        WorldBlockFace that = (WorldBlockFace) o;
        return light == that.light && blockFaceDefinition.equals(that.blockFaceDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockFaceDefinition, light);
    }
}
