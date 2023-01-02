package chunk;

import entity.Component;
import entity.Entity;

public class ChunkModelDataComponent extends Component {

    public float[] positions;
    public float[] textureCoordinates;

    public ChunkModelDataComponent(float[] positions, float[] textureCoordinates) {
        this.positions = positions;
        this.textureCoordinates = textureCoordinates;
    }

    @Override public void start() {}
    @Override public void stop() {}
    @Override public void apply(Entity entity) {}
}
