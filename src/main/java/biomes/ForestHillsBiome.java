package biomes;

import biome.Biome;
import block.Block;
import structures.Flower;
import structures.Tree;
import structures.WildPotato;

public class ForestHillsBiome extends Biome {

    @Override
    public float getRoughness() { return 0.20f; }

    @Override
    public float getBaselineHeight() { return 110f; }

    @Override
    public float getAmplitude() { return 15f; }

    @Override
    public StructureSpawnInfo[] getStructures() {
        return new StructureSpawnInfo[] {
            new StructureSpawnInfo(Block.GRASS, 0.01f, new Tree()),
            new StructureSpawnInfo(Block.GRASS, 0.003f, new WildPotato()),
            new StructureSpawnInfo(Block.GRASS, 0.01f, new Flower())
        };
    }

    @Override
    public String toString() {
        return "Forest";
    }

}
