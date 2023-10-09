package chunk;

import entity.Component;
import entity.Entity;
import org.joml.Vector3i;

import java.util.List;

public class ChunkBlockEntityDataComponent extends Component {

    private List<Vector3i> blockEntityLocations;

    public ChunkBlockEntityDataComponent(List<Vector3i> blockEntityLocations) {
        this.blockEntityLocations = blockEntityLocations;
    }

    public List<Vector3i> getBlockEntityLocations() {
        return blockEntityLocations;
    }

    @Override
    public void apply(Entity entity) {

    }

    @Override
    public void destroy(Entity entity) {

    }
}
