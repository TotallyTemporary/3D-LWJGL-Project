package entity;

import chunk.CardinalDirection;
import render.Model;

public class ChunkModelComponent extends ModelComponent {

    private final int[] positionsIndices;

    public ChunkModelComponent(Model model, int[] positionsIndices) {
        super(model);
        this.positionsIndices = positionsIndices;
    }

    // the functions below returns values that can directly be used in glDrawArrays

    public int getPositionIndex(int face) {
        if (face >= CardinalDirection.COUNT) return model.getVertexCount();
        return positionsIndices[face];
    }
}
