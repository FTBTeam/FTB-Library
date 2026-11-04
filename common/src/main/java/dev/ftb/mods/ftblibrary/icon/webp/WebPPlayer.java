package dev.ftb.mods.ftblibrary.icon.webp;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Per-viewer playback state for an animation: play/pause/restart, frame timing and loop handling. This class is
 * pure logic with no rendering dependencies. It is driven by {@link #update(long)} on the render thread.
 * <p>
 * Off-screen pausing is implicit: if {@code update()} isn't called for longer than {@link #MAX_GAP_NANOS} (a widget
 * which isn't being drawn), the gap is discarded rather than skipping frames, so playback resumes where it left off.
 */
public class WebPPlayer implements AutoCloseable {
    /** Gaps between updates longer than this are treated as "not being displayed" and don't advance the animation */
    public static final long MAX_GAP_NANOS = 250_000_000L;
    /** Browsers clamp very short frame delays; we do likewise to avoid runaway frame advance */
    public static final int MIN_FRAME_MS = 20;
    public static final int DEFAULT_FRAME_MS = 100;

    private final Supplier<WebPFrameFeed> feedFactory;
    private final int loopCount;
    private final int frameCount;

    private @Nullable WebPFrameFeed feed;
    private @Nullable WebPFrameData current;
    private int currentIndex;
    private long elapsedNanos;
    private long lastUpdateNanos;
    private boolean hasLastUpdate;
    private int completedLoops;
    private boolean playing = true;
    private boolean finished;
    private boolean newLoop;

    /**
     * @param loopCount   total plays; 0 = loop forever
     * @param frameCount  number of frames in one pass (for progress reporting)
     * @param feedFactory creates a fresh feed starting from the first frame
     */
    public WebPPlayer(int loopCount, int frameCount, Supplier<WebPFrameFeed> feedFactory) {
        this.loopCount = loopCount;
        this.frameCount = Math.max(1, frameCount);
        this.feedFactory = feedFactory;
    }

    public static WebPPlayer forAnimation(WebPAnimation animation) {
        return new WebPPlayer(animation.getLoopCount(), animation.getFrameCount(), animation::openFeed);
    }

    /**
     * Advance playback to the given time.
     *
     * @param nowNanos monotonic time, e.g. {@code System.nanoTime()}
     * @return the frame to display, or null if the first frame isn't decoded yet
     */
    public @Nullable WebPFrameData update(long nowNanos) {
        long delta = hasLastUpdate ? nowNanos - lastUpdateNanos : 0;
        lastUpdateNanos = nowNanos;
        hasLastUpdate = true;
        if (delta < 0 || delta > MAX_GAP_NANOS) {
            delta = 0;
        }

        if (feed == null) {
            feed = feedFactory.get();
        }

        if (current == null) {
            WebPFrameData first = feed.poll();
            if (first == null) {
                return null;
            }
            current = first;
            currentIndex = 0;
            elapsedNanos = 0;
        }

        if (!playing || finished) {
            return current;
        }

        elapsedNanos += delta;
        while (elapsedNanos >= frameNanos(current)) {
            WebPFrameData next = feed.poll();
            if (next != null) {
                elapsedNanos -= frameNanos(current);
                current = next;
                currentIndex = newLoop ? 0 : currentIndex + 1;
                newLoop = false;
            } else if (feed.isFinished()) {
                if (loopCount == 0 || completedLoops + 1 < loopCount) {
                    completedLoops++;
                    feed.close();
                    feed = feedFactory.get();
                    newLoop = true;
                    // next iteration polls the new feed; the last frame stays up until its first frame is decoded
                } else {
                    finished = true;
                    elapsedNanos = 0;
                    break;
                }
            } else {
                // decoder hasn't caught up; hold the current frame rather than skipping time
                elapsedNanos = frameNanos(current);
                break;
            }
        }
        return current;
    }

    private static long frameNanos(WebPFrameData frame) {
        int ms = frame.durationMs();
        if (ms <= 0) {
            ms = DEFAULT_FRAME_MS;
        }
        return Math.max(ms, MIN_FRAME_MS) * 1_000_000L;
    }

    public boolean isPlaying() {
        return playing && !finished;
    }

    /** True when the animation has played its declared number of loops and is holding on the last frame */
    public boolean isFinished() {
        return finished;
    }

    public void play() {
        if (finished) {
            restart();
        }
        playing = true;
    }

    public void pause() {
        playing = false;
    }

    public void toggle() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    /** Start again from the first frame and resume playing */
    public void restart() {
        if (feed != null) {
            feed.close();
            feed = null;
        }
        current = null;
        currentIndex = 0;
        elapsedNanos = 0;
        completedLoops = 0;
        finished = false;
        playing = true;
        newLoop = false;
    }

    /** @return playback position within the current loop, 0..1 (approximate: by frame index) */
    public float getProgress() {
        return Math.min(1f, (currentIndex + 1f) / frameCount);
    }

    @Override
    public void close() {
        if (feed != null) {
            feed.close();
            feed = null;
        }
        current = null;
    }
}
