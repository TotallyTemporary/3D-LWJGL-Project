package chunk;

import entity.Component;
import entity.Entity;

public class ChunkModelDataComponent extends Component {

    // these contain the entire position (vec3) and textureCoords (vec3) data of a chunk.
    public float[] positions;
    public float[] textureCoordinates;

    // the arrays above are "subdivided", so that they're ordered by face direction, going UP,LEFT,FRONT,BACK,RIGHT,DOWN. (CardinalDirection.java)
    // the arrays below contain the indices at which each of these directions resides.
    // Example: to render only UP faces, render from positionsIndices[0] to positionsIndices[1], pass these to glDrawArrays (offset and length).
    public int[]   positionsIndices;

    public ChunkModelDataComponent(float[] positions, float[] textureCoordinates, int[] positionsIndices) {
        this.positions = positions;
        this.textureCoordinates = textureCoordinates;
        this.positionsIndices = positionsIndices;
    }

    @Override public void start() {}
    @Override public void stop() {}
    @Override public void apply(Entity entity) {}
}
