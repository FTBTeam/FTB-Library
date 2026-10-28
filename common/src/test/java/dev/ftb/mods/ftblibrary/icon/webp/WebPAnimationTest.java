package dev.ftb.mods.ftblibrary.icon.webp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebPAnimationTest {
    private static byte[] load(String name) throws IOException {
        try (InputStream in = WebPAnimationTest.class.getResourceAsStream("/webp/" + name)) {
            assertNotNull(in, name);
            return in.readAllBytes();
        }
    }

    private static List<WebPFrameData> drain(WebPFrameFeed feed) throws InterruptedException {
        List<WebPFrameData> out = new ArrayList<>();
        long deadline = System.currentTimeMillis() + 10_000;
        while (!feed.isFinished() && System.currentTimeMillis() < deadline) {
            WebPFrameData f = feed.poll();
            if (f != null) {
                out.add(f);
            } else {
                Thread.sleep(1);
            }
        }
        feed.close();
        return out;
    }

    @Test
    void parsesAnimatedHeader() throws Exception {
        var anim = WebPAnimation.parse(load("loop3.webp"));
        assertTrue(anim.isAnimated());
        assertEquals(64, anim.getWidth());
        assertEquals(64, anim.getHeight());
        assertEquals(10, anim.getFrameCount());
        assertEquals(3, anim.getLoopCount());
    }

    @Test
    void parsesInfiniteLoop() throws Exception {
        assertEquals(0, WebPAnimation.parse(load("loop_forever.webp")).getLoopCount());
    }

    @Test
    void stillIsNotAnimated() throws Exception {
        var anim = WebPAnimation.parse(load("still.webp"));
        assertFalse(anim.isAnimated());
        assertEquals(64 * 64, anim.decodeFirstFrame().length);
    }

    @Test
    void feedDeliversAllFramesThenFinishes() throws Exception {
        var anim = WebPAnimation.parse(load("loop3.webp"));
        var frames = drain(anim.openFeed());
        assertEquals(10, frames.size());
        for (var f : frames) {
            assertEquals(64 * 64, f.argb().length);
            assertEquals(100, f.durationMs());
        }
    }

    @Test
    void feedCanBeReopenedForLooping() throws Exception {
        var anim = WebPAnimation.parse(load("loop_forever.webp"));
        assertEquals(drain(anim.openFeed()).size(), drain(anim.openFeed()).size());
    }

    @Test
    void realPlayerCompletesFiniteLoops() throws Exception {
        var anim = WebPAnimation.parse(load("loop3.webp"));
        var player = WebPPlayer.forAnimation(anim);
        long t = System.nanoTime();
        long deadline = System.currentTimeMillis() + 10_000;
        // drive the player in simulated 100ms steps (with real waits for the decoder thread)
        while (!player.isFinished() && System.currentTimeMillis() < deadline) {
            player.update(t);
            t += 100_000_000L;
            Thread.sleep(2);
        }
        assertTrue(player.isFinished());
        player.close();
    }

    @Test
    void rejectsGarbage() {
        assertThrows(IOException.class, () -> WebPAnimation.parse(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}));
    }
}
