package entity;

import render.Model;

/** Component wrapper for Model.
 * @see Model
 * */
public class ModelComponent extends Component {

    protected Model model;

    public ModelComponent(Model model) {
        this.model = model;
    }

    @Override public void apply(Entity entity) {}

    public Model getModel() {
        return model;
    }
}
