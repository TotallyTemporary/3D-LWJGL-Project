package main;

import chunk.CardinalDirection;
import org.joml.Vector3f;

public class AABB {

    private Vector3f pos;
    private float width, height, depth;

    public AABB(Vector3f pos, float width, float height, float depth) {
        this.pos = pos;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }
    
    public Vector3f[] getTestPoints(int direction) {
        return switch (direction) {
            case CardinalDirection.UP -> new Vector3f[] {
                new Vector3f(pos.x + width/2f, pos.y + height, pos.z + depth/2f),
                new Vector3f(pos.x - width/2f, pos.y + height, pos.z + depth/2f),
                new Vector3f(pos.x + width/2f, pos.y + height, pos.z - depth/2f),
                new Vector3f(pos.x - width/2f, pos.y + height, pos.z - depth/2f)
            };

            case CardinalDirection.DOWN -> new Vector3f[] {
                new Vector3f(pos.x + width/2f, pos.y, pos.z + depth/2f),
                new Vector3f(pos.x - width/2f, pos.y, pos.z + depth/2f),
                new Vector3f(pos.x + width/2f, pos.y, pos.z - depth/2f),
                new Vector3f(pos.x - width/2f, pos.y, pos.z - depth/2f)
            };

            case CardinalDirection.LEFT -> new Vector3f[] {
                new Vector3f(pos.x - width/2f, pos.y + height, pos.z + depth/2f),
                new Vector3f(pos.x - width/2f, pos.y + height, pos.z - depth/2f),
                new Vector3f(pos.x - width/2f, pos.y         , pos.z + depth/2f),
                new Vector3f(pos.x - width/2f, pos.y         , pos.z - depth/2f)
            };

            case CardinalDirection.RIGHT -> new Vector3f[] {
                new Vector3f(pos.x + width/2f, pos.y + height, pos.z + depth/2f),
                new Vector3f(pos.x + width/2f, pos.y + height, pos.z - depth/2f),
                new Vector3f(pos.x + width/2f, pos.y         , pos.z + depth/2f),
                new Vector3f(pos.x + width/2f, pos.y         , pos.z - depth/2f)
            };

            case CardinalDirection.FRONT -> new Vector3f[] {
                new Vector3f(pos.x + width/2f, pos.y + height, pos.z - depth/2f),
                new Vector3f(pos.x - width/2f, pos.y + height, pos.z - depth/2f),
                new Vector3f(pos.x + width/2f, pos.y         , pos.z - depth/2f),
                new Vector3f(pos.x - width/2f, pos.y         , pos.z - depth/2f)
            };

            case CardinalDirection.BACK -> new Vector3f[] {
                new Vector3f(pos.x + width/2f, pos.y + height, pos.z + depth/2f),
                new Vector3f(pos.x - width/2f, pos.y + height, pos.z + depth/2f),
                new Vector3f(pos.x + width/2f, pos.y         , pos.z + depth/2f),
                new Vector3f(pos.x - width/2f, pos.y         , pos.z + depth/2f)
            };

            default -> null;
        };
    }
}
