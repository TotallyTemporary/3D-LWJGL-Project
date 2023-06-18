package chunk;

import entity.Component;
import entity.Entity;

/** Contains data that can be loaded into an OpenGL model.
 * It's stored separately, because the model loading into OpenGL must happen on the main thread,
 * but generating the data can happen in a thread pool. */
public class ChunkModelDataComponent extends Component {

    // these contain the entire position (vec3), textureCoords (vec3), and light (vec2) data of a chunk.
    public final float[] positions;
    public final float[] textureCoordinates;
    public final float[] light;

    // the arrays above are "subdivided", so that they're ordered by face direction, going UP,LEFT,FRONT,BACK,RIGHT,DOWN. (CardinalDirection.java)
    // the array below contains the start of each such subdivision
    // Example: to render only UP faces, vertices start at positionIndices[0] and end at positionIndices[1].
    public final int[]   positionsIndices;

    public ChunkModelDataComponent(float[] positions, float[] textureCoordinates, float[] light, int[] positionsIndices) {
        this.positions = positions;
        this.textureCoordinates = textureCoordinates;
        this.light = light;
        this.positionsIndices = positionsIndices;
    }

    @Override public void apply(Entity entity) {}
}
