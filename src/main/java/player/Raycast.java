package player;

import chunk.Block;
import chunk.ChunkLoader;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Raycast {

    /** Contains two positions for a raycast hit.
     * hitBlock is the block the ray hit
     * beforeHitBlock is the block directly before the hitBlock in the ray.
     *
     * Example: if you're shooting an arrow at a target and hit it,
     * hitBlock is the target's pos and beforeHitBlock is the arrow's pos.
     * */
    public static class RaycastHitData {
        public RaycastHitData(Vector3i hitBlock, Vector3i beforeHitBlock) {
            this.hitBlock = hitBlock;
            this.beforeHitBlock = beforeHitBlock;
        }

        public Vector3i hitBlock;
        public Vector3i beforeHitBlock;
    }

    /** Sends a raycast into the world.
     * @param pos The position where the ray should start
     * @param dir The direction where the ray should go (NOTE: NORMALIZE THIS OR RANGE IS WRONG)
     * @param range The maximum range of blocks this ray can go before returning null.
     * @return ray hit information, or null if ray ran out of length before hitting anything.
     * @see RaycastHitData
     * @implNote <a href="http://www.cse.yorku.ca/~amana/research/grid.pdf">Implementation based on this algorithm.</a>
     * */
    public static RaycastHitData raycast(Vector3f pos, Vector3f dir, float range) {
        if (Math.floor(pos.x) - pos.x == 0) pos.x += 1f/10_000;
        if (Math.floor(pos.y) - pos.y == 0) pos.y += 1f/10_000;
        if (Math.floor(pos.z) - pos.z == 0) pos.z += 1f/10_000;

        Vector3i ipos = new Vector3i(pos, RoundingMode.FLOOR);

        float stepX = Math.signum(dir.x);
        float stepY = Math.signum(dir.y);
        float stepZ = Math.signum(dir.z);

        float tMaxX = calcMaxT(pos.x, dir.x);
        float tMaxY = calcMaxT(pos.y, dir.y);
        float tMaxZ = calcMaxT(pos.z, dir.z);

        float tDeltaX = stepX/dir.x;
        float tDeltaY = stepY/dir.y;
        float tDeltaZ = stepZ/dir.z;

        Vector3i lastBlock = new Vector3i().set(ipos);
        int steps = 0;
        while (steps < range) {
            steps++;
            if (ChunkLoader.getBlockAt(ipos) != Block.AIR.getID()) {
                return new Raycast.RaycastHitData(ipos, lastBlock);
            }
            lastBlock.set(ipos);

            if (Math.abs(tMaxX) < Math.abs(tMaxY)) {
                if (Math.abs(tMaxX) < Math.abs(tMaxZ)) {
                    ipos.x += stepX;
                    tMaxX += tDeltaX;
                } else {
                    ipos.z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (Math.abs(tMaxY) < Math.abs(tMaxZ)) {
                    ipos.y += stepY;
                    tMaxY += tDeltaY;
                } else {
                    ipos.z += stepZ;
                    tMaxZ += tDeltaZ;
                }
            }
        }

        return null;
    }

    private static float calcMaxT(float pos, float vec) {
        if (vec > 0) return ((float) Math.ceil(pos) - pos) / Math.abs(vec);
        return -((float) Math.floor(pos) - pos) / Math.abs(vec);
    }

}
