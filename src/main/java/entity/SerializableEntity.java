package entity;

import java.io.*;

public interface SerializableEntity {

    void serialize(DataOutputStream out) throws IOException;
    void deserialize(DataInputStream in) throws IOException;
    int getType();
}
