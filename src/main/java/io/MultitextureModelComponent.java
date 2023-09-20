package io;

import entity.ModelComponent;
import org.joml.Vector2i;
import render.Model;

public class MultitextureModelComponent extends ModelComponent {

    private MultitextureModelPart[] parts;

    public MultitextureModelComponent(Model model, MultitextureModelPart[] parts) {
        super(model);
        this.parts = parts;
    }

    public int getPartCount() {
        return parts.length;
    }

    public MultitextureModelPart getPart(int part) {
        return this.parts[part];
    }
}
