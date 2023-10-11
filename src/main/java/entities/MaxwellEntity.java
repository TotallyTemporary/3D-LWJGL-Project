package entities;

import ai.BasicAIComponent;
import ai.SpinComponent;
import entity.*;
import io.MultitextureModelComponent;
import io.OBJFileParser;
import org.joml.Vector3f;
import player.PhysicsObjectComponent;

import java.io.*;
import java.nio.file.Path;

public class MaxwellEntity extends Entity implements SerializableEntity {

    private static MultitextureModelComponent maxwellModel = OBJFileParser.loadModel(Path.of("src/main/resources/mobs/maxwell/maxwell.obj"));

    public MaxwellEntity() {
        this(new Vector3f()); // only to call `deserialize()` right after
    }

    public MaxwellEntity(Vector3f position) {
        super();
        EntityManager.addComponent(this, maxwellModel);
        EntityManager.addComponent(this, new TransformationComponent(
                position,
                new Vector3f(),
                new Vector3f(1f / 16, 1f / 16, 1f / 16)));
        EntityManager.addComponent(this, new BasicAIComponent());
        EntityManager.addComponent(this, new PhysicsObjectComponent(new Vector3f(1.25f, 1.25f, 1.25f)));
        EntityManager.addComponent(this, new SpinComponent());
        EntityManager.addComponent(this, new SerializableEntityComponent());
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        // write transform
        var transform = EntityManager.getComponent(this, TransformationComponent.class);
        transform.serialize(out);
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        // read transform
        var transform = new TransformationComponent();
        transform.deserialize(in);
        EntityManager.addComponent(this, transform); // replace existing component
    }

    @Override
    public int getType() {
        return EntityType.MAXWELL;
    }
}
