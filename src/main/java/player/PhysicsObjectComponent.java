package player;

import block.Block;
import block.CardinalDirection;
import chunk.ChunkLoader;
import entity.Component;
import entity.Entity;
import entity.EntityManager;
import entity.TransformationComponent;
import main.AABB;
import main.Timer;
import org.joml.Vector3f;

public class PhysicsObjectComponent extends Component {

    public static final float GRAVITY = -25f,
                              DAMPING = 0.98f,

                              SMALL_OFFSET = 1f / 2_000,
                              MAX_TOTAL_DISTANCE = 100f;

    public Vector3f
        acceleration = new Vector3f(),
        velocity     = new Vector3f();

    // this is a bit of a hack for user input.
    public Vector3f altVelocity = new Vector3f();

    private float width, height, depth;
    private boolean grounded = false;
    private Runnable doOnSqueeze = () -> {};

    public PhysicsObjectComponent(Vector3f dimensions) {
        this.width = dimensions.x;
        this.height = dimensions.y;
        this.depth = dimensions.z;
    }

    public void setActionOnSqueeze(Runnable action) {
        this.doOnSqueeze = action;
    }
    
    @Override public void apply(Entity entity) {
        velocity.add(
                acceleration.mul(Timer.getFrametimeSeconds(), new Vector3f())
        );

        velocity.mul((float) Math.pow(DAMPING, Timer.getFrametimeSeconds() * 60f));

        // calculate physics
        var transform = EntityManager.getComponent(entity, TransformationComponent.class);
        assert transform != null;
        var pos = transform.getPosition();

        Vector3f deltaPos = velocity.mul(Timer.getFrametimeSeconds(), new Vector3f())
                .add(altVelocity.mul(Timer.getFrametimeSeconds()));

        // A bit of a failsafe, if the game is stuck for a long time, the deltaPos might be huge thanks to the Timer multiplication
        // so we limit it to a maximum magnitude
        if (deltaPos.length() > MAX_TOTAL_DISTANCE) {
            System.err.println("deltaPos had length " + deltaPos.length() + ", limited to " + MAX_TOTAL_DISTANCE);
            deltaPos.div(deltaPos.length() / MAX_TOTAL_DISTANCE);
        }

        float maxStepDistance = Math.min(Math.min(width, height), depth);

        // if deltaPos is too big, we might need to calculate our movement multiple times in this for loop.
        int steps = (int) (deltaPos.length() / maxStepDistance + 1);
        var deltaPosStepped = deltaPos.div(steps);
        for (int i = 0; i < steps; i++) {
            resolve(pos, deltaPosStepped);
        }

        acceleration = new Vector3f(0, GRAVITY, 0);
    }

    private void resolve(Vector3f pos, Vector3f deltaPos) {
        // TODO always resolving Y first leads to some problems, do calculate timeY and compare that with the other axis.
        // we always resolve Y first, then we see if we should resolve X or Z next.
        resolveY(pos, deltaPos);

        // this calculates how quickly our X pos or Z pos reaches the next block edge (whole number)
        // since deltaPos is guaranteed to be less than 1 block's length, this is a good estimate.

        float timeX;
        if (deltaPos.x > 0) timeX = (1 - frac(pos.x + width/2f)) / deltaPos.x;
        else                timeX = (    frac(pos.x - width/2f)) / deltaPos.x;
        if (Float.isNaN(timeX)) timeX = Float.POSITIVE_INFINITY;

        float timeZ;
        if (deltaPos.z > 0) timeZ = (1 - frac(pos.z + depth/2f)) / deltaPos.z;
        else                timeZ = (    frac(pos.z - depth/2f)) / deltaPos.z;
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

        var box = new AABB(new Vector3f(pos.x, pos.y+deltaPos.y, pos.z),
                                        width, height, depth);

        grounded = false;

        // get the 4 bottom points of our bounding box
        for (var point : box.getTestPoints(CardinalDirection.DOWN)) {
            if (Block.getBlock(ChunkLoader.getBlockAt(point)).isSolid()) {
                grounded = true; // TODO this sometimes fails even if we're on the ground :/

                // if y is 2.75 and we're inside the block, we want delta to be 0.25 + small offset.
                var delta = 1 - frac(point.y) + SMALL_OFFSET;
                // we get the max of these deltas, since if 3 of our corners have no obstruction but 1 does, we obey the 1 that does.
                if (delta > max) max = delta;
            }
        }

        // get the 4 top points of our bounding box
        for (var point : box.getTestPoints(CardinalDirection.UP)) {
            if (Block.getBlock(ChunkLoader.getBlockAt(point)).isSolid()) {
                // if y is 2.15, delta is -0.15 - small offset
                var delta = -frac(point.y) - SMALL_OFFSET;
                if (delta < min) min = delta;
            }
        }

        pos.y += (deltaPos.y + max + min);

        if (min != 0 || max != 0) {
            velocity.y = 0;
        }

        // if both max and min are nonzero, we're being squeezed in some way. there's no logic to deal with it now, so the physics may explode.
        if (max != 0 && min != 0) {
            doOnSqueeze.run();
        }
    }

    // see resolveY for comments
    private void resolveX(Vector3f pos, Vector3f deltaPos) {
        var max = 0f;
        var min = 0f;
        var box = new AABB(new Vector3f(pos.x+deltaPos.x, pos.y, pos.z), width, height, depth);
        for (var point : box.getTestPoints(CardinalDirection.LEFT)) {
            if (Block.getBlock(ChunkLoader.getBlockAt(point)).isSolid()) {
                var delta = 1 - frac(point.x) + SMALL_OFFSET;
                if (delta > max) max = delta;
            }
        }
        for (var point : box.getTestPoints(CardinalDirection.RIGHT)) {
            if (Block.getBlock(ChunkLoader.getBlockAt(point)).isSolid()) {
                var delta = -frac(point.x) - SMALL_OFFSET;
                if (delta < min) min = delta;
            }
        }

        pos.x += (deltaPos.x + max + min);
        if (max != 0 || min != 0) {
            velocity.x = 0;
        }

        if (max != 0 && min != 0) {
            doOnSqueeze.run();
        }
    }

    // see resolveY for comments
    private void resolveZ(Vector3f pos, Vector3f deltaPos) {
        var max = 0f;
        var min = 0f;
        var box = new AABB(new Vector3f(pos.x, pos.y, pos.z+deltaPos.z), width, height, depth);
        for (var point : box.getTestPoints(CardinalDirection.FRONT)) {
            if (Block.getBlock(ChunkLoader.getBlockAt(point)).isSolid()) {
                var delta = 1 - frac(point.z) + SMALL_OFFSET;
                if (delta > max) max = delta;
            }
        }
        for (var point : box.getTestPoints(CardinalDirection.BACK)) {
            if (Block.getBlock(ChunkLoader.getBlockAt(point)).isSolid()) {
                var delta = -frac(point.z) - SMALL_OFFSET;
                if (delta < min) min = delta;
            }
        }

        pos.z += (deltaPos.z + max + min);
        if (max != 0 || min != 0) {
            velocity.z = 0;
        }

        if (max != 0 && min != 0) {
            doOnSqueeze.run();
        }
    }

    private float frac(float f) {
        return f - (float) Math.floor(f);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getAcceleration() {
        return acceleration;
    }

    public boolean isGrounded() {
        return grounded;
    }

    @Override
    public void destroy(Entity entity) {}
}
