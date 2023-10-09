package block;

import entity.Component;
import entity.Entity;

/** This component simply removes the BlockEntity from its list if it gets destroyed.
 * This is required because entities don't really exist, only their components do.
 * Without this the BlockEntity would forever remain in its list. */
public class BlockEntityComponent extends Component {

    @Override
    public void apply(Entity entity) {

    }

    @Override
    public void destroy(Entity entity) {
        BlockEntity casted = (BlockEntity) entity;
        casted.removeSelf();
    }
}
