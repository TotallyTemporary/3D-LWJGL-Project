package chunk;

import entity.Entity;
import org.joml.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A chunk is a 32x32x32 ({@link #SIZE}) area of space which contains blocks.
 * */
public class Chunk extends Entity {

    // static stuff at the top here
    public final static int SIZE_BITS = 5;
    public final static int SIZE = 1 << SIZE_BITS;
    public final static int MAX_LIGHT = 15;

    public static Vector3i worldPosToChunkPos(Vector3f pos) {
        return worldPosToChunkPos(new Vector3i(pos, RoundingMode.FLOOR));
    }

    public static Vector3i worldPosToChunkPos(Vector3i pos) {
        return new Vector3i(
            pos.x >> SIZE_BITS,
            pos.y >> SIZE_BITS,
            pos.z >> SIZE_BITS
        );
    }

    public static Vector3i worldPosToBlockPos(Vector3f pos) {
        return worldPosToBlockPos(new Vector3i(pos, RoundingMode.FLOOR));
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
            pos.x + chunk.getChunkGridPos().x * SIZE,
            pos.y + chunk.getChunkGridPos().y * SIZE,
            pos.z + chunk.getChunkGridPos().z * SIZE
        );
    }

    // funcs for working with lightmap values

    public static byte getBlock(byte colour) {
        return (byte) ((colour >> 4) & 15);
    }

    public static byte getSky(byte colour) {
        return (byte) (colour & 15);
    }

    public static int toIndex(int x, int y, int z) {
        return (z << (2*SIZE_BITS)) + (y << SIZE_BITS) + x;
    }

    public static int toIndex(Vector3i pos) {
        return toIndex(pos.x, pos.y, pos.z);
    }

    public enum Status {
        NONE                    (0, false),
        BASIC_TERRAIN_GENERATING(1, true),  // generating simple blocks
        BASIC_TERRAIN_GENERATED (2, false),
        STRUCTURE_GENERATING    (3, true),  // generating structures (needs neighbors for bleed-over)
        BLOCKS_GENERATED        (4, false),
        LIGHTS_GENERATING       (5, true),  // generating light map
        LIGHTS_GENERATED        (6, false),
        MESH_GENERATING         (7, true),  // make the vertices and texture coords for the chunk
        MESH_GENERATED          (8, false),
        MESH_LOADING            (9, true),  // load those vertices into opengl (main thread)
        FINAL                   (10, false); // chunk can be rendered

        public final int urgency;
        public final boolean working;
        Status(int urgency, boolean working) { this.urgency = urgency; this.working = working; }
    }

    // chunk instance stuff below

    private final Vector3i chunkGridPos; // the chunk's position in the grid. the chunk's neighboring chunks differ from this by 1.
    private Status status;
    private byte[] blocks; // if isAllAir = true, then blocks = null.
    private byte[] lightMap;
    private final List<WeakReference<Chunk>> neighbors = new ArrayList<>(Collections.nCopies(DiagonalDirection.COUNT, null)); // 26 chunk neighbors

    public boolean spoiled = false;
    private boolean isAirChunk = false; // use this to fill chunk with skylight

    public Chunk(Vector3i chunkGridPos) {
        super();
        this.chunkGridPos = chunkGridPos;
        this.status = Status.NONE;

        // get all surrounding neighbors, add them as neighbor and add us as their neighbor.
        var index = 0;
        for (var offset : DiagonalDirection.offsets) {
            var neighborPos = offset.add(chunkGridPos, new Vector3i());
            var neighbor = ChunkLoader.getChunkAt(neighborPos);
            if (neighbor != null) {
                setNeighbor(neighbor, index);
                neighbor.setNeighbor(this, DiagonalDirection.opposite(index));
            }
            index++;
        }
    }

    public Vector3i getChunkGridPos() {
        return chunkGridPos;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public byte getColour(int x, int y, int z) {
        return lightMap[toIndex(x, y, z)];
    }

    public byte getColour(Vector3i pos) {
        return getColour(pos.x, pos.y, pos.z);
    }

    public byte getColourSafe(int x, int y, int z) {
        if (isInsideChunk(x, y, z)) {
            return this.getColour(x, y, z);
        }

        var worldPos = Chunk.blockPosToWorldPos(new Vector3i(x, y, z), this);
        var chunkPos = Chunk.worldPosToChunkPos(worldPos);
        try {
            int dirIndex = DiagonalDirection.indexOf(chunkPos.sub(this.chunkGridPos));
            return getNeighbor(dirIndex).getColour(Chunk.worldPosToBlockPos(worldPos));
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void setColourSafe(Vector3i pos, byte colour) {
        setColourSafe(pos.x, pos.y, pos.z, colour);
    }

    public void setColourSafe(int x, int y, int z, byte colour) {
        var worldPos = Chunk.blockPosToWorldPos(new Vector3i(x, y, z), this);
        var chunkPos = Chunk.worldPosToChunkPos(worldPos);

        if (chunkPos.equals(this.chunkGridPos)) {
            this.setColour(x, y, z, colour);
            return;
        }

        // get neighbor at chunkPos
        try {
            int dirIndex = DiagonalDirection.indexOf(chunkPos.sub(this.chunkGridPos));
            var neighbor = getNeighbor(dirIndex);

            var blockPos = Chunk.worldPosToBlockPos(worldPos);
            neighbor.setColourSafe(blockPos, colour);
        } catch (NullPointerException e) {
            System.err.println("setColourSafe failed across chunk border at " + worldPos);
        }
    }


    public void setColour(Vector3i pos, byte light) {
        setColour(pos.x, pos.y, pos.z, light);
    }

    public void setColour(int x, int y, int z, byte light) {
        this.lightMap[toIndex(x, y, z)] = light;
    }

    public void setColours(byte[] lightMap) {
        this.lightMap = lightMap;
    }

    public byte getBlock(int x, int y, int z) {
        return blocks[toIndex(x, y, z)];
    }

    public byte getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    /** Returns the id of the block at x,y,z.
     * Ensures given values are between 0-31. For example, if passed x=-1, this function would return the block of the neighboring chunk at x=31. */
    public byte getBlockSafe(int x, int y, int z) {
        if (isInsideChunk(x, y, z)) {
            return this.getBlock(x, y, z);
        }

        var worldPos = Chunk.blockPosToWorldPos(new Vector3i(x, y, z), this);
        var chunkPos = Chunk.worldPosToChunkPos(worldPos);
        try {
            int dirIndex = DiagonalDirection.indexOf(chunkPos.sub(this.chunkGridPos));
            return getNeighbor(dirIndex).getBlock(Chunk.worldPosToBlockPos(worldPos));
        } catch (NullPointerException e) {
            return Block.INVALID.getID();
        }
    }

    public byte getBlockSafe(Vector3i pos) {
        return getBlockSafe(pos.x, pos.y, pos.z);
    }

    public void setBlockSafe(Vector3i pos, byte block) {
        setBlockSafe(pos.x, pos.y, pos.z, block);
    }

    /** Sets a block at x,y,z. If x,y,z values are not within 0-31, they overflow or underflow to a neighboring chunk.
     * For example, given x=-1, we would set the block of a neighboring chunk at x=31.
     * @see Chunk#getBlockSafe  */
    public void setBlockSafe(int x, int y, int z, byte block) {
        var worldPos = Chunk.blockPosToWorldPos(new Vector3i(x, y, z), this);
        var chunkPos = Chunk.worldPosToChunkPos(worldPos);

        if (chunkPos.equals(this.chunkGridPos)) {
            this.setBlock(x, y, z, block);
            return;
        }

        // get neighbor at chunkPos
        try {
            int dirIndex = DiagonalDirection.indexOf(chunkPos.sub(this.chunkGridPos));
            var neighbor = getNeighbor(dirIndex);

            var blockPos = Chunk.worldPosToBlockPos(worldPos);
            neighbor.setBlockSafe(blockPos, block);
        } catch (NullPointerException e) {
            System.err.println("setBlockSafe failed across chunk border at " + worldPos);
        }
    }

    public void setBlock(Vector3i pos, byte block) {
        setBlock(pos.x, pos.y, pos.z, block);
    }

    public void setBlock(int x, int y, int z, byte block) {
        setIsAirChunk(false);
        this.blocks[Chunk.toIndex(x, y, z)] = block;
    }

    public void setBlocks(byte[] blocks) {
        this.blocks = blocks;
    }

    public boolean isInsideChunk(int x, int y, int z) {
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

    public boolean getIsAirChunk() {
        return this.isAirChunk;
    }

    public void setIsAirChunk(boolean isAirChunk) {
        if (this.isAirChunk == false && isAirChunk == true) {
            // a block is being set into an air chunk
            if (this.status.urgency >= Status.LIGHTS_GENERATING.urgency) { // regenerate light maps
                spoiled = true;
            }
        }

       this.isAirChunk = isAirChunk;
    }
}
