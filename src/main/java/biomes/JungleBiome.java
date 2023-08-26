package biomes;

import biome.Biome;
import block.Block;
import structures.Flower;
import structures.JungleTree;
import structures.SmallJungleTree;
import structures.Tree;

public class JungleBiome extends Biome {

    @Override
    public float getRoughness() { return 0.18f; }

    @Override
    public float getBaselineHeight() { return 100f; }

    @Override
    public float getAmplitude() { return 18f; }

    @Override
    public StructureSpawnInfo[] getStructures() {
        return new StructureSpawnInfo[] {
            new StructureSpawnInfo(Block.GRASS, 0.008f, new JungleTree()),
            new StructureSpawnInfo(Block.GRASS, 0.005f, new SmallJungleTree())
        };
    }

    @Override
    public String toString() {
        return "Jungle";
    }

}
