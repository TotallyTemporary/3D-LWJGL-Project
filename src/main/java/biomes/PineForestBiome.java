package biomes;

import biome.Biome;
import block.Block;
import structures.*;

public class PineForestBiome extends Biome {

    @Override
    public float getRoughness() { return 0.15f; }

    @Override
    public float getBaselineHeight() { return 90f; }

    @Override
    public float getAmplitude() { return 10f; }

    @Override
    public StructureSpawnInfo[] getStructures() {
        return new StructureSpawnInfo[] {
            new StructureSpawnInfo(Block.GRASS, 0.02f, new TallGrass()),
            new StructureSpawnInfo(Block.GRASS, 0.003f, new WildPotato()),
            new StructureSpawnInfo(Block.GRASS, 0.01f, new PineTree())
        };
    }

    @Override
    public String toString() {
        return "Pine Forest";
    }


}
