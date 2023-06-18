package biome;

import chunk.Chunk;
import org.joml.Vector3i;

import java.util.function.Supplier;

public abstract class Structure {

    public void make(Chunk chunk, Vector3i position, Supplier<Float> roll) {}

}
