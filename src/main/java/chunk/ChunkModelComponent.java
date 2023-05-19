package chunk;

import chunk.CardinalDirection;
import entity.ModelComponent;
import render.Model;

/** Contains the mesh for this chunk. Can be rendered onto the screen. */
public class ChunkModelComponent extends ModelComponent {

    /* {@see ChunkModelDataComponent#positionsIndices} for explanation */
    private final int[] positionsIndices;

    public ChunkModelComponent(Model model, int[] positionsIndices) {
        super(model);
        this.positionsIndices = positionsIndices;
    }

    public int getPositionIndex(int face) {
        if (face >= CardinalDirection.COUNT) return model.getVertexCount();
        return positionsIndices[face];
    }
}
