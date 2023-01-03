package chunk;

import entity.Entity;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Chunk extends Entity {

    // static stuff at the top here
    public final static int SIZE_BITS = 6;
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

    // UP, LEFT, FRONT, BACK, RIGHT, DOWN;
    public static Vector3i[] neighbors(Vector3i pos) {
        return new Vector3i[]{
            new Vector3i(pos.x, pos.y+1, pos.z),
            new Vector3i(pos.x-1, pos.y, pos.z),
            new Vector3i(pos.x, pos.y, pos.z-1),
            new Vector3i(pos.x, pos.y, pos.z+1),
            new Vector3i(pos.x+1, pos.y, pos.z),
            new Vector3i(pos.x, pos.y-1, pos.z),
        };
    }

    public static int toIndex(int x, int y, int z) {
        return z*SIZE*SIZE + y*SIZE + x;
    }

    public static int toIndex(Vector3i pos) {
        return toIndex(pos.x, pos.y, pos.z);
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

    // chunk instance stuff below

    private Vector3i chunkPos;
    private Status status;
    private byte[] blocks;
    private List<WeakReference<Chunk>> neighbors = new ArrayList<>(Collections.nCopies(6, (WeakReference<Chunk>) null));

    public Chunk(Vector3i chunkPos) {
        super();
        this.chunkPos = chunkPos;
        this.status = Status.NONE;

        // get all surrounding neighbors, add them as neighbor and add us as their neighbor.
        var index = 0;
        for (var neighborPos : neighbors(chunkPos)) {
            var neighbor = ChunkLoader.getChunkAt(neighborPos);
            if (neighbor != null) {
                setNeighbor(neighbor, index);
                neighbor.setNeighbor(this, Direction.opposite(index));
            }
            index++;
        }
    }

    public Vector3i getChunkPos() {
        return chunkPos;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        // System.out.println(this.chunkPos + ", " + status);
        this.status = status;
    }

    public byte getBlock(int x, int y, int z) {
        if (!isInsideChunk(x, y, z)) return Block.INVALID.getID();
        else return blocks[toIndex(x, y, z)];
    }

    public byte getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    public void setBlocks(byte[] blocks) {
        this.blocks = blocks;
    }

    private boolean isInsideChunk(int x, int y, int z) {
        if (x >= SIZE || x < 0 ||
            y >= SIZE || y < 0 ||
            z >= SIZE || z < 0) return false;
        return true;
    }

    public Chunk getNeighbor(int index) {
        var ref = neighbors.get(index);
        if (ref == null) return null;
        else return ref.get();
    }

    public void setNeighbor(Chunk chunk, int index) {
        neighbors.set(index, new WeakReference<>(chunk));
    }
}
