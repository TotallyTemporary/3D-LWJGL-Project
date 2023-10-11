package entity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface SerializableComponent {
    void serialize(DataOutputStream out) throws IOException;
    void deserialize(DataInputStream in) throws IOException;
}
