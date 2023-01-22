package player;

import chunk.Block;
import chunk.ChunkLoader;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Raycast {

    public static class RaycastHitData {
        public RaycastHitData(Vector3i hitBlock, Vector3i beforeHitBlock) {
            this.hitBlock = hitBlock;
            this.beforeHitBlock = beforeHitBlock;
        }

        public Vector3i hitBlock;
        public Vector3i beforeHitBlock;
    }

    public static RaycastHitData raycast(Vector3f pos, Vector3f t, float range) {
        if (Math.floor(pos.x) - pos.x == 0) pos.x += 1f/10_000;
        if (Math.floor(pos.y) - pos.y == 0) pos.y += 1f/10_000;
        if (Math.floor(pos.z) - pos.z == 0) pos.z += 1f/10_000;

        Vector3i ipos = new Vector3i(pos, RoundingMode.FLOOR);

        float stepX = Math.signum(t.x);
        float stepY = Math.signum(t.y);
        float stepZ = Math.signum(t.z);

        float tMaxX = calcMaxT(pos.x, t.x);
        float tMaxY = calcMaxT(pos.y, t.y);
        float tMaxZ = calcMaxT(pos.z, t.z);

        float tDeltaX = stepX/t.x;
        float tDeltaY = stepY/t.y;
        float tDeltaZ = stepZ/t.z;

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
