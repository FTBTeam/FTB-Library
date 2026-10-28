package dev.ftb.mods.ftblibrary.icon.webp;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebPPlayerTest {
    private static final long MS = 1_000_000L;

    /** Feed handing out frames tagged by their pixel value; all available immediately */
    private static class FakeFeed implements WebPFrameFeed {
        private final int frames;
        private final int durationMs;
        private int next;
        boolean closed;

        FakeFeed(int frames, int durationMs) {
            this.frames = frames;
            this.durationMs = durationMs;
        }

        @Override
        public @Nullable WebPFrameData poll() {
            if (next >= frames) {
                return null;
            }
            return new WebPFrameData(new int[]{next++}, durationMs);
        }

        @Override
        public boolean isFinished() {
            return next >= frames;
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private static int tag(@Nullable WebPFrameData f) {
        assertNotNull(f);
        return f.argb()[0];
    }

    private static WebPPlayer player(int loops, int frames, int durationMs, List<FakeFeed> created) {
        return new WebPPlayer(loops, frames, () -> {
            FakeFeed f = new FakeFeed(frames, durationMs);
            created.add(f);
            return f;
        });
    }

    @Test
    void advancesByFrameDuration() {
        var p = player(0, 3, 100, new ArrayList<>());
        long t = 0;
        assertEquals(0, tag(p.update(t)));
        assertEquals(0, tag(p.update(t += 50 * MS)));
        assertEquals(1, tag(p.update(t += 60 * MS)));
        assertEquals(2, tag(p.update(t += 100 * MS)));
    }

    @Test
    void loopsForever() {
        var feeds = new ArrayList<FakeFeed>();
        var p = player(0, 2, 100, feeds);
        long t = 0;
        p.update(t);
        for (int i = 0; i < 10; i++) {
            p.update(t += 100 * MS);
        }
        assertFalse(p.isFinished());
        assertTrue(feeds.size() > 1);
    }

    @Test
    void honoursFiniteLoopCount() {
        var p = player(2, 2, 100, new ArrayList<>());
        long t = 0;
        p.update(t);
        // 2 loops x 2 frames x 100ms: after well over 400ms it should have stopped on the last frame
        for (int i = 0; i < 10; i++) {
            p.update(t += 100 * MS);
        }
        assertTrue(p.isFinished());
        assertFalse(p.isPlaying());
        assertEquals(1, tag(p.update(t += 100 * MS)));
    }

    @Test
    void pauseHoldsFrameAndResumeContinues() {
        var p = player(0, 4, 100, new ArrayList<>());
        long t = 0;
        p.update(t);
        assertEquals(1, tag(p.update(t += 100 * MS)));
        p.pause();
        assertEquals(1, tag(p.update(t += 100 * MS)));
        assertEquals(1, tag(p.update(t += 100 * MS)));
        p.play();
        assertEquals(1, tag(p.update(t += 10 * MS)));
        assertEquals(2, tag(p.update(t += 100 * MS)));
    }

    @Test
    void longGapDoesNotSkipFrames() {
        // simulates the widget being off-screen (not drawn) for a while
        var p = player(0, 5, 100, new ArrayList<>());
        long t = 0;
        p.update(t);
        assertEquals(1, tag(p.update(t += 100 * MS)));
        assertEquals(1, tag(p.update(t += 10_000 * MS)));
        assertEquals(2, tag(p.update(t += 100 * MS)));
    }

    @Test
    void restartGoesBackToFirstFrameAndClosesFeed() {
        var feeds = new ArrayList<FakeFeed>();
        var p = player(0, 4, 100, feeds);
        long t = 0;
        p.update(t);
        p.update(t += 200 * MS);
        p.restart();
        assertTrue(feeds.get(0).closed);
        assertEquals(0, tag(p.update(t += 10 * MS)));
        assertTrue(p.isPlaying());
    }

    @Test
    void restartAfterFinishedPlaysAgain() {
        var p = player(1, 2, 100, new ArrayList<>());
        long t = 0;
        p.update(t);
        for (int i = 0; i < 5; i++) {
            p.update(t += 100 * MS);
        }
        assertTrue(p.isFinished());
        p.toggle(); // play() on a finished animation restarts it
        assertEquals(0, tag(p.update(t += 10 * MS)));
        assertTrue(p.isPlaying());
    }

    @Test
    void waitsForSlowDecoderWithoutSkipping() {
        var feed = new WebPFrameFeed() {
            final List<WebPFrameData> ready = new ArrayList<>(List.of(new WebPFrameData(new int[]{0}, 100)));
            int idx;

            @Override
            public @Nullable WebPFrameData poll() {
                return idx < ready.size() ? ready.get(idx++) : null;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public void close() {
            }
        };
        var p = new WebPPlayer(0, 3, () -> feed);
        long t = 0;
        assertEquals(0, tag(p.update(t)));
        assertEquals(0, tag(p.update(t += 200 * MS)));  // decoder behind: hold frame 0
        feed.ready.add(new WebPFrameData(new int[]{1}, 100));
        assertEquals(1, tag(p.update(t += 10 * MS)));    // catches up on the next update
    }

    @Test
    void zeroDurationFramesDoNotSpin() {
        var p = player(0, 5, 0, new ArrayList<>());
        long t = 0;
        p.update(t);
        // zero durations are treated as 100ms, so 10ms later we're still on frame 0
        assertEquals(0, tag(p.update(t += 10 * MS)));
    }
}
