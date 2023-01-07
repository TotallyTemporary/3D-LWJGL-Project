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

    public static Vector3i worldPosToChunkPos(Vector3i pos) {
        return new Vector3i(
                pos.x >> SIZE_BITS,
                pos.y >> SIZE_BITS,
                pos.z >> SIZE_BITS
        );
    }

    public static Vector3i worldPosToBlockPos(Vector3f pos) {
        return new Vector3i(
            (int) pos.x & (SIZE-1),
            (int) pos.y & (SIZE-1),
            (int) pos.z & (SIZE-1)
        );
    }

    public static Vector3i worldPosToBlockPos(Vector3i pos) {
        return new Vector3i(
                pos.x & (SIZE-1),
                pos.y & (SIZE-1),
                pos.z & (SIZE-1)
        );
    }

    public static Vector3i blockPosToWorldPos(Vector3i pos, Chunk chunk) {
        return new Vector3i(
            pos.x + chunk.getChunkPos().x * SIZE,
            pos.y + chunk.getChunkPos().y * SIZE,
            pos.z + chunk.getChunkPos().z * SIZE
        );
    }

    public static int toIndex(int x, int y, int z) {
        return z*SIZE*SIZE + y*SIZE + x;
    }

    public static int toIndex(Vector3i pos) {
        return toIndex(pos.x, pos.y, pos.z);
    }

    public enum Status {
        NONE(0),
        TERRAIN_GENERATING(1),    // generating simple blocks
        WAIT_NEIGHBORS(2),
        STRUCTURE_GENERATING(3),  // generating structures (needs neighbors for bleed-over)
        LOADED(4),

        MESH_GENERATING(5),       // make the vertices and texture coords for the chunk
        PREPARED(6),
        MESH_LOADING(7),          // load those vertices into opengl (main thread)
        FINAL(8);                 // chunk can be rendered

        public int urgency;
        private Status(int urgency) { this.urgency = urgency; }
    }

    // chunk instance stuff below

    private Vector3i chunkPos;
    private Status status;
    private boolean isAllAir; // if a chunk is all air, we don't store blocks.
    private byte[] blocks;
    private List<WeakReference<Chunk>> neighbors = new ArrayList<>(Collections.nCopies(DiagonalDirection.COUNT, null));

    public boolean updated = false; // sorta temporary flag. used by chunkloader.

    public Chunk(Vector3i chunkPos) {
        super();
        this.chunkPos = chunkPos;
        this.status = Status.NONE;

        // get all surrounding neighbors, add them as neighbor and add us as their neighbor.
        var index = 0;
        for (var offset : DiagonalDirection.offsets) {
            var neighborPos = offset.add(chunkPos, new Vector3i());
            var neighbor = ChunkLoader.getChunkAt(neighborPos);
            if (neighbor != null) {
                setNeighbor(neighbor, index);
                neighbor.setNeighbor(this, DiagonalDirection.opposite(index));
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
        this.status = status;
    }

    public byte getBlock(int x, int y, int z) {
        if (isAllAir) return Block.AIR.getID();
        else return blocks[toIndex(x, y, z)];
    }

    public byte getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    public byte getBlockSafe(int x, int y, int z) {
        if (isInsideChunk(x, y, z)) {
            return this.getBlock(x, y, z);
        }

        var worldPos = Chunk.blockPosToWorldPos(new Vector3i(x, y, z), this);
        var chunkPos = Chunk.worldPosToChunkPos(worldPos);

        int dirIndex = DiagonalDirection.indexOf(chunkPos.sub(this.chunkPos));
        return neighbors.get(
            dirIndex
        ).get().getBlock(Chunk.worldPosToBlockPos(worldPos));
    }

    public byte getBlockSafe(Vector3i pos) {
        return getBlockSafe(pos.x, pos.y, pos.z);
    }

    public void setBlockSafe(Vector3i pos, byte block) {
        setBlockSafe(pos.x, pos.y, pos.z, block);
    }

    public void setBlockSafe(int x, int y, int z, byte block) {
        if (this.isAllAir) {
            this.blocks = new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE];
            this.isAllAir = false;
        }

        var worldPos = Chunk.blockPosToWorldPos(new Vector3i(x, y, z), this);
        var chunkPos = Chunk.worldPosToChunkPos(worldPos);

        if (chunkPos.equals(this.chunkPos)) {
            this.setBlock(x, y, z, block);
            return;
        }

        int dirIndex = DiagonalDirection.indexOf(chunkPos.sub(this.chunkPos));
        var neighbor = neighbors.get(dirIndex).get();
        var blockPos = Chunk.worldPosToBlockPos(worldPos);
        neighbor.setBlockSafe(blockPos, block);
    }

    public void setBlock(Vector3i pos, byte block) {
        setBlock(pos.x, pos.y, pos.z, block);
    }

    public void setBlock(int x, int y, int z, byte block) {
        this.blocks[Chunk.toIndex(x, y, z)] = block;
    }

    public void setBlocks(byte[] blocks) {
        this.blocks = blocks;
    }

    public void isAllAir() {
        this.isAllAir = true;
    }

    private boolean isInsideChunk(int x, int y, int z) {
        return x < SIZE && x >= 0 &&
                y < SIZE && y >= 0 &&
                z < SIZE && z >= 0;
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
