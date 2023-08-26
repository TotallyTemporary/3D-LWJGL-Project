package biomes;

import biome.Biome;
import org.joml.Vector2f;

import java.util.HashMap;

public class Biomes {

    // moisture, temperature
    public static HashMap<Vector2f, Biome> biomes = new HashMap();
    static {
        biomes.put(new Vector2f(0.10f, 0.95f), new DesertBiome());
        biomes.put(new Vector2f(0.05f, 0.65f), new DesertBiome());
        biomes.put(new Vector2f(0.05f, 0.10f), new TundraBiome());
        // biomes.put(new Vector2f(0.25f, 0.70f), new SavannaBiome()); // TODO savanna
        // biomes.put(new Vector2f(0.25f, 0.90f), new DryWoodlandBiome()); // TODO shrubland
        biomes.put(new Vector2f(0.10f, 0.50f), new PlainsBiome());
        biomes.put(new Vector2f(0.30f, 0.50f), new PineForestBiome()); // boreal forest
        biomes.put(new Vector2f(0.50f, 0.70f), new ForestHillsBiome()); // temperate forest
        // biomes.put(new Vector2f(0.50f, 0.90f), new ForestHillsBiome()); // TODO tropical seasonal forest (monsoon jungle?)
        biomes.put(new Vector2f(0.75f, 0.90f), new JungleBiome()); // jungle
    }

}
