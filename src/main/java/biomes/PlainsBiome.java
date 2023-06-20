package biomes;

import biome.Biome;
import chunk.Block;
import structures.Flower;
import structures.Tree;

public class PlainsBiome extends Biome {

    @Override
    public float getRoughness() { return 0.17f; }

    @Override
    public float getBaselineHeight() { return 100f; }

    @Override
    public float getAmplitude() { return 5f; }

    @Override
    public StructureSpawnInfo[] getStructures() {
        return new StructureSpawnInfo[] {
            new StructureSpawnInfo(Block.GRASS, 0.01f, new Flower())
        };
    }

    @Override
    public String toString() {
        return "Plains";
    }
}
