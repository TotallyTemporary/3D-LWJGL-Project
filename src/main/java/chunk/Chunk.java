package chunk;

import entity.Entity;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Chunk extends Entity {

    public final static int SIZE_BITS = 4;
    public final static int SIZE = 1 << SIZE_BITS;

    public static Vector3i worldPosToChunkPos(Vector3f pos) {
        return new Vector3i(
            (int) pos.x >> SIZE_BITS,
            (int) pos.y >> SIZE_BITS,
            (int) pos.z >> SIZE_BITS
        );
    }

    public enum Status {
        NONE(0),                  // this chunk does not exist.
        TERRAIN_GEN(1),           // generating blocks
        WAIT_NEIGHBORS(2),        // wait for neighbors to generate their terrain
        LOADING(3),               // queued to load into a model in memory
        FINAL(4);                 // chunk can be rendered.

        public int urgency;
        private Status(int urgency) { this.urgency = urgency; }
    }

    private Vector3i chunkPos;
    private Status status;
    private byte[][][] blocks;

    public Chunk(Vector3i chunkPos) {
        super();
        this.chunkPos = chunkPos;
        this.status = Status.NONE;
    }

    public Vector3i getChunkPos() {
        return chunkPos;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        System.out.println(chunkPos + ": " + status);
        this.status = status;
    }

    public byte[][][] getBlocks() {
        return blocks;
    }

    public void setBlocks(byte[][][] blocks) {
        this.blocks = blocks;
    }
}
