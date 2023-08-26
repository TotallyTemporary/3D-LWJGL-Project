package biomes;

import biome.Biome;
import block.Block;

public class TundraBiome extends Biome {

    @Override
    public float getRoughness() { return 0.17f; }

    @Override
    public float getBaselineHeight() { return 100f; }

    @Override
    public float getAmplitude() { return 5f; }

    @Override
    protected Block[] getSurfaceBlocks() { return new Block[] { Block.SNOWY_GRASS }; }


    @Override
    public StructureSpawnInfo[] getStructures() {
        return new StructureSpawnInfo[] {};
    }

    @Override
    public String toString() {
        return "Tundra";
    }
}
