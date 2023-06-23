package chunk;

import block.Block;
import block.CardinalDirection;
import block.DiagonalDirection;
import org.joml.Vector3i;
import org.joml.Vector4i;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class LightMapGenerator {

    // may only have 1 thread running
    private static Thread thread = new Thread(LightMapGenerator::run);
    private static boolean running = true;
    static { thread.start(); }

    private static BlockingQueue<Chunk> loadQueue = new LinkedBlockingQueue<>();

    public static void addChunk(Chunk chunk) {
        loadQueue.add(chunk);
    }

    public static int getQueueSize() {
        return loadQueue.size();
    }

    public static void stop() {
        running = false;
        try {
            thread.interrupt();
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void run() {
        System.out.println("LightMapGenerator running");
        while (running) {
            try {
                var chunk = loadQueue.take();
                loadChunk(chunk);
            } catch (InterruptedException e) {}
        }
        System.out.println("LightMapGenerator stopped");
    }

    public static void loadChunk(Chunk chunk) {
        if (chunk.getIsAirChunk()) {
            loadAllAirChunk(chunk);
            chunk.setStatus(Chunk.Status.LIGHTS_GENERATED);
            return;
        }

        // lightmap generation is done in two phases
        // 1. take the skylight from chunks surrounding you and propagate them to yourself
        // 2. take the skylight from the 3x3 chunks above you, and propagate them to yourself and chunk surrounding you
        chunk.setColours(new byte[Chunk.SIZE * Chunk.SIZE * Chunk.SIZE]);

        var spoiledChunks = new ArrayList<Chunk>();

        propagateNeighborLight(chunk, spoiledChunks);
        propagateAboveLight(chunk, spoiledChunks);

        for (Chunk neighbor : spoiledChunks) {
            if (!neighbor.spoiled) {
                System.out.println("spoiled");
                neighbor.spoiled = true;
            }
        }
        chunk.setStatus(Chunk.Status.LIGHTS_GENERATED);
    }

    private static void loadAllAirChunk(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE; x++)
        for (int y = 0; y < Chunk.SIZE; y++)
        for (int z = 0; z < Chunk.SIZE; z++)
        {
            chunk.setColour(x, y, z, (byte) Chunk.MAX_LIGHT);
        }

        for (int dir = 0; dir < DiagonalDirection.COUNT; dir++) {
            Chunk neighbor = chunk.getNeighbor(dir);
            if (!neighbor.getIsAirChunk()
                && neighbor.getStatus().urgency > Chunk.Status.LIGHTS_GENERATED.urgency) {
                neighbor.spoiled = true; // TODO might cause deadlock
            }
        }
    }

    private static void propagateAboveLight(Chunk chunk, ArrayList<Chunk> spoiledChunks) {
        Queue<Vector4i> lightSources = new ArrayDeque<>();
        getAboveLightSources(chunk, lightSources);

        propagateInArea(chunk, lightSources, spoiledChunks, v -> isPositionIn3x3Grid(v.x, v.y, v.z));
    }

    private static void propagateNeighborLight(Chunk chunk, ArrayList<Chunk> spoiledChunks) {
        Queue<Vector4i> lightSources = new ArrayDeque<>();
        getNeighborLightSources(chunk, lightSources);

        propagateInArea(chunk, lightSources, spoiledChunks, v -> chunk.isInsideChunk(v.x, v.y, v.z));
    }

    private static void getAboveLightSources(Chunk chunk, Queue<Vector4i> locs) {
        for (int x = -Chunk.MAX_LIGHT - 1; x < Chunk.SIZE + Chunk.MAX_LIGHT; x++)
        for (int z = -Chunk.MAX_LIGHT - 1; z < Chunk.SIZE + Chunk.MAX_LIGHT; z++)
        {
            int y = Chunk.SIZE + 1;

            byte light = getSkyAt(chunk, x, y, z);
            if (light == 0) {
                continue;
            }
            if (light != Chunk.MAX_LIGHT) {
                light -= 1;
            }

            locs.add(new Vector4i(x, y-1, z, light));
        }
    }

    private static boolean isPositionIn3x3Grid(int x, int y, int z) {
        return (x < -Chunk.MAX_LIGHT - 1 || x >= Chunk.SIZE + Chunk.MAX_LIGHT)
            || (z < -Chunk.MAX_LIGHT - 1 || z >= Chunk.SIZE + Chunk.MAX_LIGHT)
            || (y < -Chunk.SIZE + 1      || y > Chunk.SIZE + 1);
    }

    private static void getNeighborLightSources(Chunk chunk, Queue<Vector4i> locs) {
        for (int dir = 0; dir < CardinalDirection.COUNT; dir++)
        for (int axis1 = 0; axis1 < Chunk.SIZE; axis1++)
        for (int axis2 = 0; axis2 < Chunk.SIZE; axis2++)
        {
            int x = 0, y = 0, z = 0;

            switch (dir) {
                case CardinalDirection.UP:
                    x = axis1;
                    z = axis2;
                    y = Chunk.SIZE;
                    break;
                case CardinalDirection.DOWN:
                    x = axis1;
                    z = axis2;
                    y = -1;
                    break;
                case CardinalDirection.FRONT:
                    x = axis1;
                    y = axis2;
                    z = -1;
                    break;
                case CardinalDirection.BACK:
                    x = axis1;
                    y = axis2;
                    z = Chunk.SIZE;
                    break;
                case CardinalDirection.LEFT:
                    y = axis1;
                    z = axis2;
                    x = -1;
                    break;
                case CardinalDirection.RIGHT:
                    y = axis1;
                    z = axis2;
                    x = Chunk.SIZE;
                    break;
            }

            byte light = getSkyAt(chunk, x, y, z);
            if (light == 0) {
                continue;
            }
            if (light != Chunk.MAX_LIGHT) {
                light -= 1;
            }

            var dirOffset = CardinalDirection.offsets[dir];
            locs.add(new Vector4i(x-dirOffset.x, y-dirOffset.y, z-dirOffset.z, light));
        }
    }

    private static void propagateInArea(Chunk chunk,
                                        Queue<Vector4i> sources,
                                        ArrayList<Chunk> spoiledChunks,
                                        Function<Vector3i, Boolean> areaPredicate) {
        while (!sources.isEmpty()) {
            Vector4i source = sources.poll();

            // set colour
            byte light = chunk.getColourSafe(source.x, source.y, source.z);
            if (light >= source.w) continue;
            chunk.setColourSafe(source.x, source.y, source.z, (byte) source.w);

            // propagate
            for (int dir = 0; dir < CardinalDirection.COUNT; dir++) {
                var offset = CardinalDirection.offsets[dir];

                int nx = source.x + offset.x;
                int ny = source.y + offset.y;
                int nz = source.z + offset.z;
                byte nl;
                if (source.w == Chunk.MAX_LIGHT && dir == CardinalDirection.DOWN) {
                    // skylight does not attenuate moving down if it's at full intensity
                    nl = (byte) (source.w);
                } else {
                    nl = (byte) (source.w - 1);
                }

                int existingSkyLight = getSkyAt(chunk, nx, ny, nz);
                boolean isInArea = areaPredicate.apply(new Vector3i(nx, ny, nz));

                if (nl <= 0 || existingSkyLight >= nl) {
                    continue;
                }

                // spoil neighboring chunks as we move through them or near their border
                if (!chunk.isInsideChunk(nx, ny, nz)) {
                    Chunk neighbor = getChunkAtRelativeCoords(chunk, nx, ny, nz);
                    if (neighbor.getStatus().urgency > Chunk.Status.LIGHTS_GENERATED.urgency
                            && !neighbor.spoiled) {
                        spoiledChunks.add(neighbor);
                    }
                }

                if (!canLightPassThroughAt(chunk, nx, ny, nz) || !isInArea) {
                    continue;
                }

                sources.add(new Vector4i(nx, ny, nz, nl));
            }
        }
    }

    private static Chunk getChunkAtRelativeCoords(Chunk chunk, int x, int y, int z) {
        var worldPos = Chunk.blockPosToWorldPos(new Vector3i(x, y, z), chunk);
        var chunkPos = Chunk.worldPosToChunkPos(worldPos);
        int dirIndex = DiagonalDirection.indexOf(chunkPos.sub(chunk.getChunkGridPos()));
        return chunk.getNeighbor(dirIndex);
    }

    private static boolean canLightPassThroughAt(Chunk chunk, int x, int y, int z) {
        byte block = chunk.getBlockSafe(x, y, z);
        return Block.getBlock(block).getHasTransparentFace();
    }

    private static byte getSkyAt(Chunk chunk, int x, int y, int z) {
        byte sky = Chunk.getSky(chunk.getColourSafe(x, y, z));
        return sky;
    }
}
