package entity;

import render.Model;

public class ModelComponent extends Component {

    private Model model;

    public ModelComponent(Model model) {
        this.model = model;
    }

    @Override public void start() {}
    @Override public void stop() {}
    @Override public void apply(Entity entity) {}

    public Model getModel() {
        return model;
    }
}
