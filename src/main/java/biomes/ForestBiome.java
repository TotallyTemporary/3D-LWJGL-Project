package biomes;

import biome.Biome;
import chunk.Block;
import structures.Flower;
import structures.Tree;

public class ForestBiome extends Biome {

    @Override
    public StructureSpawnInfo[] getStructures() {
        return new StructureSpawnInfo[] {
            new StructureSpawnInfo(Block.GRASS, 0.01f, new Tree()),
            new StructureSpawnInfo(Block.GRASS, 0.01f, new Flower())
        };
    }

}
