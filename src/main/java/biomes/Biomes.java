package biomes;

import biome.Biome;
import org.joml.Vector2f;

import java.util.HashMap;

public class Biomes {

    // moisture, temperature
    public static HashMap<Vector2f, Biome> biomes = new HashMap();
    static {
        biomes.put(new Vector2f(0.75f, 0.70f), new PlainsBiome());
        biomes.put(new Vector2f(0.80f, 0.80f), new ForestHillsBiome());
        biomes.put(new Vector2f(0.20f, 0.95f), new DesertBiome());
    }

}
