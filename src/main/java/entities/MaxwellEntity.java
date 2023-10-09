package entities;

import ai.BasicAIComponent;
import ai.SpinComponent;
import entity.*;
import io.MultitextureModelComponent;
import io.OBJFileParser;
import org.joml.Vector3f;
import player.PhysicsObjectComponent;
import render.Model;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class MaxwellEntity extends Entity implements SerializableEntity {

    private static MultitextureModelComponent maxwellModel = OBJFileParser.loadModel(Path.of("src/main/resources/mobs/maxwell/maxwell.obj"));

    public MaxwellEntity() {
        // only call to call 'unserialize' right after
    }

    public MaxwellEntity(Vector3f position) {
        super();
        init(position, new Vector3f());
    }

    private void init(Vector3f position, Vector3f rotation) {
        EntityManager.addComponent(this, maxwellModel);
        EntityManager.addComponent(this, new TransformationComponent(
                position,
                rotation,
                new Vector3f(1f / 16, 1f / 16, 1f / 16)));
        EntityManager.addComponent(this, new BasicAIComponent());
        EntityManager.addComponent(this, new PhysicsObjectComponent(new Vector3f(1.25f, 1.25f, 1.25f)));
        EntityManager.addComponent(this, new SpinComponent());
        EntityManager.addComponent(this, new SerializableComponent());
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        // write transform
        var transform = EntityManager.getComponent(this, TransformationComponent.class);
        var pos = transform.getPosition();
        out.writeFloat(pos.x);
        out.writeFloat(pos.y);
        out.writeFloat(pos.z);

        var rot = transform.getRotation();
        out.writeFloat(rot.x);
        out.writeFloat(rot.y);
        out.writeFloat(rot.z);

        out.flush();
    }

    @Override
    public void deserialize(DataInputStream in) throws IOException {
        // read transform
        var pos = new Vector3f();
        pos.x = in.readFloat();
        pos.y = in.readFloat();
        pos.z = in.readFloat();

        var rot = new Vector3f();
        rot.x = in.readFloat();
        rot.y = in.readFloat();
        rot.z = in.readFloat();

        init(pos, rot);
    }

    @Override
    public int getType() {
        return EntityType.MAXWELL;
    }
}
