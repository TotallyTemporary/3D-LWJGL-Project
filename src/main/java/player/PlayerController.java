package player;

import chunk.Block;
import chunk.CardinalDirection;
import chunk.ChunkLoader;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import main.Keyboard;
import main.Timer;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class PlayerController extends Component {
    // player bounds
    public static final float
        WIDTH  = 0.75f,
        DEPTH = 0.75f,
        HEIGHT = 1.75f,
        EYE_LEVEL = 1.5f;

    public static final float
        MOVE_SPEED = 25f,
        GRAVITY = -9.81f;

    public static final float
        SMALL_OFFSET = 0.001f, // so we're never really on the bounds of a block
        MAX_MOVE_DISTANCE = new Vector3f(0.5f, 0.5f, 0.5f).length();

    @Override public void start() {}

    @Override public void stop() {}

    @Override public void apply(Entity entity) {
        // get player position
        var transComp = EntityManager.getComponent(entity, TransformationComponent.class);
        Vector3f pos = transComp.getPosition();

        // get player input (desired change in position)
        Vector3f deltaPos = getInput(transComp);
        deltaPos.y += GRAVITY;
        deltaPos.mul(Timer.getFrametimeSeconds());

        // if deltaPos is too big, we might need to calculate our movement multiple times in this for loop.
        int steps = (int) (deltaPos.length() / MAX_MOVE_DISTANCE + 1);
        var deltaPosStepped = deltaPos.div(steps);
        for (int i = 0; i < steps; i++) {
            resolve(pos, deltaPosStepped);
        }
    }

    private void resolve(Vector3f pos, Vector3f deltaPos) {
        // we always resolve Y first, then we see if we should resolve X or Z next.
        resolveY(pos, deltaPos);

        // this calculates how quickly our X pos or Z pos reaches the next block edge (whole number)
        // since deltaPos is guaranteed to be less than 1 block's length, this is a good estimate.
        float timeX;
        if (deltaPos.x > 0) timeX = (1 - (pos.x + WIDTH/2f)%1) / deltaPos.x;
        else                timeX = (    (pos.x - WIDTH/2f)%1) / deltaPos.x;
        if (Float.isNaN(timeX)) timeX = Float.POSITIVE_INFINITY;

        float timeZ;
        if (deltaPos.z > 0) timeZ = (1 - (pos.z + DEPTH/2f)%1) / deltaPos.z;
        else                timeZ = (    (pos.z - DEPTH/2f)%1) / deltaPos.z;
        if (Float.isNaN(timeZ)) timeZ = Float.POSITIVE_INFINITY;

        // if x reaches block edge first, resolve x first.
        if (timeX < timeZ) {
            resolveX(pos, deltaPos);
            resolveZ(pos, deltaPos);
        } else {
            resolveZ(pos, deltaPos);
            resolveX(pos, deltaPos);
        }
    }

    // this logic is basically duplicated for X and Z, so it will only be commented here.
    private void resolveY(Vector3f pos, Vector3f deltaPos) {
        var max = 0f; // how much should we move up (testing down dir)
        var min = 0f; // how much should we move down (testing up dir)

        // get the 4 points at the corners of our bounding box, DOWN.
        for (var point : getTestablePointsInDirection(new Vector3f(pos.x, pos.y+deltaPos.y, pos.z), CardinalDirection.DOWN)) {
            if (ChunkLoader.getBlockAt(point) != Block.AIR.getID()) {
                // float % 1 is fractional part. you can see how (1 - point.y%1) is the distance from point to the next whole number above it.
                var delta = 1 - point.y % 1 + SMALL_OFFSET;

                // we get the max of these deltas, since if 3 of our corners have no obstruction but 1 does, we obey the 1 that does.
                if (delta > max) max = delta;
            }
        }
        for (var point : getTestablePointsInDirection(new Vector3f(pos.x, pos.y+deltaPos.y, pos.z), CardinalDirection.UP)) {
            if (ChunkLoader.getBlockAt(point) != Block.AIR.getID()) {
                var delta = -(point.y % 1) - SMALL_OFFSET;
                if (delta < min) min = delta;
            }
        }

        // if both max and min are nonzero, we're being squeezed in some way. there's no logic to deal with it now, so the physics may explode.
        pos.y += (deltaPos.y + max + min);
    }

    private void resolveX(Vector3f pos, Vector3f deltaPos) {
        var max = 0f;
        var min = 0f;
        for (var point : getTestablePointsInDirection(new Vector3f(pos.x+deltaPos.x, pos.y, pos.z), CardinalDirection.LEFT)) {
            if (ChunkLoader.getBlockAt(point) != Block.AIR.getID()) {
                var delta = 1 - point.x % 1 + SMALL_OFFSET;
                if (delta > max)
                    max = delta;
            }
        }
        for (var point : getTestablePointsInDirection(new Vector3f(pos.x+deltaPos.x, pos.y, pos.z), CardinalDirection.RIGHT)) {
            if (ChunkLoader.getBlockAt(point) != Block.AIR.getID()) {
                var delta = -(point.x % 1) - SMALL_OFFSET;
                if (delta < min) min = delta;
            }
        }

        pos.x += (deltaPos.x + max + min);
    }

    private void resolveZ(Vector3f pos, Vector3f deltaPos) {
        var max = 0f;
        var min = 0f;
        for (var point : getTestablePointsInDirection(new Vector3f(pos.x, pos.y, pos.z+deltaPos.z), CardinalDirection.FRONT)) {
            if (ChunkLoader.getBlockAt(point) != Block.AIR.getID()) {
                var delta = 1 - point.z % 1 + SMALL_OFFSET;
                if (delta > max) max = delta;
            }
        }
        for (var point : getTestablePointsInDirection(new Vector3f(pos.x, pos.y, pos.z+deltaPos.z), CardinalDirection.BACK)) {
            if (ChunkLoader.getBlockAt(point) != Block.AIR.getID()) {
                var delta = -(point.z % 1) - SMALL_OFFSET;
                if (delta < min) min = delta;
            }
        }

        pos.z += (deltaPos.z + max + min);
    }

    // returns position change, also updates rotation directly to the component.
    private Vector3f getInput(TransformationComponent transform) {
        // update rotation
        float ROT_SPEED = 2f * Timer.getFrametimeSeconds();
        var rot = transform.getRotation();
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT))  rot.y += ROT_SPEED;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT)) rot.y -= ROT_SPEED;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_UP))    rot.x += ROT_SPEED;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_DOWN))  rot.x -= ROT_SPEED;

        // update position
        float front = 0;
        float left  = 0;
        float up = 0;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_W)) front += 1;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_S)) front -= 1;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_A)) left  += 1;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_D)) left  -= 1;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE))      up += 1;
        if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) up -= 1;


        float angle = transform.getRotation().y;

        var forwardVector = new Vector3f(0, 0, -front).rotateY(angle);
        var sidewayVector = new Vector3f(-left,  0, 0).rotateY((float) (angle + 2*Math.PI));
        var comb = forwardVector.add(sidewayVector).add(0, up, 0);
        if (comb.equals(0, 0, 0)) return comb;
        return comb.normalize().mul(MOVE_SPEED);
    }

    // basically returns the corners of our player's bounding box in a certain direction,
    // example: when going upwards, it returns the top 4 points on our head.
    private Vector3f[] getTestablePointsInDirection(Vector3f pos, int direction) {
        return switch (direction) {
            case CardinalDirection.UP -> new Vector3f[] {
                    new Vector3f(pos.x + WIDTH/2f, pos.y + HEIGHT, pos.z + DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y + HEIGHT, pos.z + DEPTH/2f),
                    new Vector3f(pos.x + WIDTH/2f, pos.y + HEIGHT, pos.z - DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y + HEIGHT, pos.z - DEPTH/2f)
            };

            case CardinalDirection.DOWN -> new Vector3f[] {
                    new Vector3f(pos.x + WIDTH/2f, pos.y, pos.z + DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y, pos.z + DEPTH/2f),
                    new Vector3f(pos.x + WIDTH/2f, pos.y, pos.z - DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y, pos.z - DEPTH/2f)
            };

            case CardinalDirection.LEFT -> new Vector3f[] {
                    new Vector3f(pos.x - WIDTH/2f, pos.y + HEIGHT, pos.z + DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y + HEIGHT, pos.z - DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y         , pos.z + DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y         , pos.z - DEPTH/2f)
            };

            case CardinalDirection.RIGHT -> new Vector3f[] {
                    new Vector3f(pos.x + WIDTH/2f, pos.y + HEIGHT, pos.z + DEPTH/2f),
                    new Vector3f(pos.x + WIDTH/2f, pos.y + HEIGHT, pos.z - DEPTH/2f),
                    new Vector3f(pos.x + WIDTH/2f, pos.y         , pos.z + DEPTH/2f),
                    new Vector3f(pos.x + WIDTH/2f, pos.y         , pos.z - DEPTH/2f)
            };

            case CardinalDirection.FRONT -> new Vector3f[] {
                    new Vector3f(pos.x + WIDTH/2f, pos.y + HEIGHT, pos.z - DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y + HEIGHT, pos.z - DEPTH/2f),
                    new Vector3f(pos.x + WIDTH/2f, pos.y         , pos.z - DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y         , pos.z - DEPTH/2f)
            };

            case CardinalDirection.BACK -> new Vector3f[] {
                    new Vector3f(pos.x + WIDTH/2f, pos.y + HEIGHT, pos.z + DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y + HEIGHT, pos.z + DEPTH/2f),
                    new Vector3f(pos.x + WIDTH/2f, pos.y         , pos.z + DEPTH/2f),
                    new Vector3f(pos.x - WIDTH/2f, pos.y         , pos.z + DEPTH/2f)
            };

            default -> null;
        };
    }
}
