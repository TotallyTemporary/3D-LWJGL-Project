package main;

import chunk.CardinalDirection;
import org.joml.Vector3f;

/** Axis Aligned Bounding Box
 * Used for physics.
 * */
public class AABB {

    private Vector3f pos;
    private float width, height, depth;

    /** Construct an axis-aligned bounding box
     * @param pos Represents the origin of this bounding box, at the bottom center of the box.
     * for a player model, you might consider this point to be in between their legs on the ground.
     * @param width total width of the box in the x-direction, the box spans origin-width/2 to origin+width/2.
     * @param height height of the box in the y-direction, see: param width
     * @param depth depth of the box in the z-direction, see: param width */
    public AABB(Vector3f pos, float width, float height, float depth) {
        this.pos = pos;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /** This function returns the points of this bounding box in a particular cardinal direction.
     * Example: if direction is DOWN, return the bottom 4 points of this bounding box. */
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

            default -> throw new IllegalArgumentException("Direction must be CardinalDirection, got " + direction
                        + " which is >= " + CardinalDirection.COUNT);
        };
    }
}
