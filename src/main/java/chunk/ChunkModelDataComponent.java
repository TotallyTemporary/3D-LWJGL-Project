package chunk;

import entity.Component;
import entity.Entity;

public class ChunkModelDataComponent extends Component {

    // these contain the entire position (vec3) and textureCoords (vec3) data of a chunk.
    public final float[] positions;
    public final float[] textureCoordinates;

    // the arrays above are "subdivided", so that they're ordered by face direction, going UP,LEFT,FRONT,BACK,RIGHT,DOWN. (CardinalDirection.java)
    // the array below contains the start of each such subdivision
    // Example: to render only UP faces, vertices start at positionIndices[0] and end at positionIndices[1].
    public final int[]   positionsIndices;

    public ChunkModelDataComponent(float[] positions, float[] textureCoordinates, int[] positionsIndices) {
        this.positions = positions;
        this.textureCoordinates = textureCoordinates;
        this.positionsIndices = positionsIndices;
    }

    @Override public void apply(Entity entity) {}
}
