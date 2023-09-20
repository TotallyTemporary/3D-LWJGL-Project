package io;

import render.Texture;

public class MultitextureModelPart {

    private Texture texture;
    private int startVertex;
    private int vertexCount;

    public MultitextureModelPart(Texture texture, int startVertex, int vertexCount) {
        this.texture = texture;
        this.startVertex = startVertex;
        this.vertexCount = vertexCount;
    }

    public Texture getTexture() {
        return texture;
    }

    public int getStartVertex() {
        return startVertex;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
