package biome;

import chunk.Block;
import structures.Tree;

import java.util.function.Supplier;

public abstract class Biome {
    public record StructureSpawnInfo(Block spawnBlock, float chance, Structure structure) {}

    public float getBaselineHeight() { return 120f; }
    public float getAmplitude() { return 20f; }
    public float getRoughness() { return 0.20f; }

    public StructureSpawnInfo[] getStructures() {
        return new StructureSpawnInfo[] {};
    }

    protected Block[] getSurfaceBlocks() { return new Block[] { Block.GRASS }; }
    protected Block[] getBelowSurfaceBlocks() { return new Block[] { Block.DIRT }; }
    protected Block[] getDefaultOreBlocks() { return new Block[] { Block.STONE }; }


    public Block getBlock(int depth, Supplier<Float> roll) {
        switch (depth) {
            case 0: return chooseBlock(getSurfaceBlocks(), roll);
            case 1:
            case 2:
            case 3:
                return chooseBlock(getBelowSurfaceBlocks(), roll);
            default:
                return getOre(depth, roll, chooseBlock(getDefaultOreBlocks(), roll));
        }
    }

    private static Block chooseBlock(Block[] blocks, Supplier<Float> roll) {
        int index = (int) (roll.get() * blocks.length);
        return blocks[index];
    }

    private static Block getOre(int depth, Supplier<Float> roll, Block baseBlock) {
        if      (roll.get() < oreChance(depth, -120f, 0.01f, -30f, 0.001f)) return Block.DIAMOND_ORE;
        else if (roll.get() < oreChance(depth, -120f, 0.05f, -30f, 0.005f)) return Block.GOLD_ORE;
        else if (roll.get() < oreChance(depth, -20f, 0.1f, 20f, 0.07f)) return Block.IRON_ORE;
        else if (roll.get() < oreChance(depth, -20f, 0.1f, 20f, 0.07f)) return Block.COAL_ORE;
        else return baseBlock;
    }

    protected static float oreChance(float depth, float deep, float deepChance, float high, float highChance) {
        if (depth < deep) return deepChance;
        if (depth > high) return highChance;
        float ratio = (depth - deep) / (high - deep);
        return highChance + smoothstep(1f - ratio) * deepChance;
    }

    private static float smoothstep(float x) {
        return (float) (6*Math.pow(x, 5) - 15*Math.pow(x, 4) + 10*Math.pow(x, 3));
    }
}
