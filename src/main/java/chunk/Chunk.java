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

    public static Vector3i worldPosToBlockPos(Vector3f pos) {
        return new Vector3i(
            (int) pos.x & (SIZE-1),
            (int) pos.y & (SIZE-1),
            (int) pos.z & (SIZE-1)
        );
    }

    public static Vector3i blockPosToWorldPos(Vector3i pos, Chunk chunk) {
        return new Vector3i(
            pos.x + chunk.getChunkPos().x * SIZE,
            pos.y + chunk.getChunkPos().y * SIZE,
            pos.z + chunk.getChunkPos().z * SIZE
        );
    }

    public enum Status {
        NONE(0),                  // this chunk does not exist.
        TERRAIN_GENERATING(1),    // generating blocks
        WAIT_NEIGHBORS(2),        // wait for neighbors to generate their terrain
        MESH_GENERATING(3),       // generating vertices and other model data
        PREPARED(4),              // vertices done
        MESH_LOADING(5),          // queued to load into a model in memory
        FINAL(6);                 // chunk can be rendered.

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
        this.status = status;
    }

    public byte getBlock(int x, int y, int z) {
        if (!isInsideChunk(x, y, z)) return Block.INVALID.getID();
        else return blocks[x][y][z];
    }

    public byte getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    public void setBlocks(byte[][][] blocks) {
        this.blocks = blocks;
    }

    private boolean isInsideChunk(int x, int y, int z) {
        if (x >= SIZE || x < 0 ||
            y >= SIZE || y < 0 ||
            z >= SIZE || z < 0) return false;
        return true;
    }
}
