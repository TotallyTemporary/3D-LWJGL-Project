package biomes;

import biome.Biome;
import block.Block;
import structures.Flower;
import structures.TallGrass;
import structures.WildCarrot;

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
            new StructureSpawnInfo(Block.GRASS, 0.001f, new WildCarrot()),
            new StructureSpawnInfo(Block.GRASS, 0.01f, new Flower()),
            new StructureSpawnInfo(Block.GRASS, 0.02f, new TallGrass())
        };
    }

    @Override
    public String toString() {
        return "Plains";
    }
}
