package dev.ftb.mods.ftblibrary.icon.webp;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import org.glavo.webp.WebPFrame;
import org.glavo.webp.WebPImageReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An immutable, shareable handle on an encoded WebP image (still or animated). Only the encoded bytes and header
 * information are retained; frames are decoded on demand via {@link #openFeed()} so memory use stays bounded
 * regardless of animation length.
 */
public final class WebPAnimation {
    public static final int MAX_ANIMATED_PIXELS = 2048 * 2048;
    public static final int MAX_STATIC_PIXELS = 4096 * 4096;
    /** Number of decoded frames buffered ahead of the player */
    private static final int FEED_BUFFER = 3;

    private final byte[] data;
    private final int width;
    private final int height;
    private final boolean animated;
    private final int frameCount;
    private final int loopCount;

    private WebPAnimation(byte[] data, int width, int height, boolean animated, int frameCount, int loopCount) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.animated = animated;
        this.frameCount = frameCount;
        this.loopCount = loopCount;
    }

    /**
     * Parse the header of an encoded WebP image, without decoding any pixels.
     *
     * @throws IOException if the data is not valid WebP, or exceeds the size limits
     */
    public static WebPAnimation parse(byte[] data) throws IOException {
        try (WebPImageReader reader = WebPImageReader.open(new ByteArrayInputStream(data))) {
            long pixels = (long) reader.getWidth() * reader.getHeight();
            long limit = reader.isAnimated() ? MAX_ANIMATED_PIXELS : MAX_STATIC_PIXELS;
            if (pixels <= 0 || pixels > limit) {
                throw new IOException("WebP image too large: " + reader.getWidth() + "x" + reader.getHeight());
            }
            return new WebPAnimation(data, reader.getWidth(), reader.getHeight(), reader.isAnimated(),
                    Math.max(1, reader.getFrameCount()), reader.getLoopCount());
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isAnimated() {
        return animated && frameCount > 1;
    }

    public int getFrameCount() {
        return frameCount;
    }

    /**
     * @return the number of times the animation plays; 0 means forever. Stills report 1.
     */
    public int getLoopCount() {
        return loopCount;
    }

    /**
     * Decode just the first frame, synchronously.
     */
    public int[] decodeFirstFrame() throws IOException {
        try (WebPImageReader reader = WebPImageReader.open(new ByteArrayInputStream(data))) {
            WebPFrame frame = reader.readNextFrame();
            if (frame == null) {
                throw new IOException("WebP image has no frames");
            }
            return frame.getArgbArray();
        }
    }

    /**
     * Start decoding all frames from the beginning on a background thread. The caller must close the feed.
     */
    public WebPFrameFeed openFeed() {
        return new AsyncFeed(data);
    }

    private static final class AsyncFeed implements WebPFrameFeed {
        private static final WebPFrameData END = new WebPFrameData(new int[0], 0);
        private static int threadCounter = 0;

        private final BlockingQueue<WebPFrameData> queue = new ArrayBlockingQueue<>(FEED_BUFFER);
        private final Thread thread;
        private volatile boolean closed;
        private boolean finished;

        AsyncFeed(byte[] data) {
            thread = new Thread(() -> decodeLoop(data), "FTBLibrary WebP decoder #" + (threadCounter++ & 0xFFFF));
            thread.setDaemon(true);
            thread.start();
        }

        private void decodeLoop(byte[] data) {
            try (WebPImageReader reader = WebPImageReader.open(new ByteArrayInputStream(data))) {
                WebPFrame frame;
                while (!closed && (frame = reader.readNextFrame()) != null) {
                    WebPFrameData fd = new WebPFrameData(frame.getArgbArray(), frame.getDurationMillis());
                    // blocks while the buffer is full, which also throttles decoding while the widget is off-screen
                    while (!closed && !queue.offer(fd, 100, TimeUnit.MILLISECONDS)) {
                        // retry until accepted or closed
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception | OutOfMemoryError e) {
                if (!closed) {
                    FTBLibrary.LOGGER.warn("Failed to decode WebP frame", e);
                }
            } finally {
                try {
                    while (!closed && !queue.offer(END, 100, TimeUnit.MILLISECONDS)) {
                        // retry
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public WebPFrameData poll() {
            if (finished) {
                return null;
            }
            WebPFrameData fd = queue.poll();
            if (fd == END) {
                finished = true;
                return null;
            }
            return fd;
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public void close() {
            closed = true;
            thread.interrupt();
            queue.clear();
        }
    }
}
