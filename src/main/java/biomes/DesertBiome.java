package biomes;

import biome.Biome;
import chunk.Block;
import structures.Cactus;

public class DesertBiome extends Biome {

    @Override
    public float getRoughness() { return 0.03f; }

    @Override
    public float getBaselineHeight() { return 100f; }

    @Override
    public float getAmplitude() { return 15f; }

    @Override
    protected Block[] getSurfaceBlocks() { return new Block[] { Block.SAND }; }

    @Override
    protected Block[] getBelowSurfaceBlocks() { return new Block[] { Block.SAND }; }

    @Override
    public StructureSpawnInfo[] getStructures() {
        return new StructureSpawnInfo[] {
            new StructureSpawnInfo(Block.SAND, 0.0005f, new Cactus())
        };
    }

    @Override
    public String toString() {
        return "Desert";
    }
}