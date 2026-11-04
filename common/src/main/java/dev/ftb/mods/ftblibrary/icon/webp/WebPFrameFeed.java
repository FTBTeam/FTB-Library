package dev.ftb.mods.ftblibrary.icon.webp;

import org.jetbrains.annotations.Nullable;

/**
 * A forward-only, non-blocking source of animation frames. The player polls this from the render thread.
 */
public interface WebPFrameFeed extends AutoCloseable {
    /**
     * @return the next frame, or null if none is ready right now (see {@link #isFinished()})
     */
    @Nullable WebPFrameData poll();

    /**
     * @return true once every frame has been handed out (or decoding failed) and {@link #poll()} will only return null
     */
    boolean isFinished();

    @Override
    void close();
}
