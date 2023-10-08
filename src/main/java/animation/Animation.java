package animation;

import entity.TransformationComponent;
import org.joml.Matrix4f;

import java.util.List;

public class Animation {

    public enum PlaybackMode {
        PLAY_ONCE,
        LOOP_FOREVER
    }

    private final List<KeyFrame> keyFrames;
    private final PlaybackMode playbackMode;

    private float lengthMillis;
    private long timeStarted;

    private boolean started = false;
    private boolean ended = false;

    private Matrix4f workingMatrix = new Matrix4f().identity();

    public Animation(List<KeyFrame> keyFrames, PlaybackMode playbackMode) {
        this.keyFrames = keyFrames;
        this.playbackMode = playbackMode;
    }

    public Animation(List<KeyFrame> keyFrames) {
        this(keyFrames, PlaybackMode.PLAY_ONCE);
    }

    public void start(float lengthMillis) {
        this.lengthMillis = lengthMillis;
        this.timeStarted = System.currentTimeMillis();
        this.started = true;
        this.ended = false;
    }

    public Matrix4f getTransformation() {
        if (!started) {
            return workingMatrix;
        }

        float time = getScalarTime();
        var frame1 = getStartFrame(time);
        var frame2 = getEndFrame(time);

        // lerp between the two matrices
        float lerpBy = getLerpFactor(time, frame1.getTime(), frame2.getTime());
        var transform = frame1.getTransformation().lerp(frame2.getTransformation(), lerpBy, workingMatrix);

        return transform;
    }

    private float getScalarTime() {
        long now = System.currentTimeMillis();
        long delta = now - timeStarted;
        float scalar = delta / lengthMillis;

        if (playbackMode == PlaybackMode.LOOP_FOREVER) {
            scalar %= 1f;
        }

        if (playbackMode != PlaybackMode.LOOP_FOREVER && scalar >= 1f) {
            ended = true;
        }

        return Math.max(0f, Math.min(1f, scalar));
    }

    /** Gets the last keyframe with a time smaller than `scalarTime`. */
    private KeyFrame getStartFrame(float scalarTime) {
        KeyFrame last = null;
        for (KeyFrame frame : keyFrames) {
            if ((frame.getTime() <= scalarTime || frame.getTime() == 0f) && frame.getTime() != 1f) {
                last = frame;
            } else {
                return last;
            }
        }
        return null;
    }

    /** Gets the first keyframe with a time larger than `scalarTime`. */
    private KeyFrame getEndFrame(float scalarTime) {
        for (KeyFrame frame : keyFrames) {
            if (frame.getTime() > scalarTime || frame.getTime() == 1f) {
                return frame;
            }
        }
        return null;
    }

    /** Linear interpolation */
    private float getLerpFactor(float time, float frame1, float frame2) {
        return (time - frame1) / (frame2 - frame1);
    }

}
