package chunk;

import biome.Biome;
import entity.Component;
import entity.Entity;

public class TerrainMapDataComponent extends Component {

    private int[][] heightMap;
    private Biome[][] biomeMap;

    public TerrainMapDataComponent(int[][] heightMap, Biome[][] biomeMap) {
        this.heightMap = heightMap;
        this.biomeMap = biomeMap;
    }

    public int[][] getHeightMap() {
        return heightMap;
    }

    public Biome[][] getBiomeMap() {
        return biomeMap;
    }

    @Override
    public void apply(Entity entity) {}

    @Override
    public void destroy(Entity entity) {}
}
