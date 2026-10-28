package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.WebPIcon;
import dev.ftb.mods.ftblibrary.icon.webp.WebPAnimation;
import dev.ftb.mods.ftblibrary.icon.webp.WebPFrameData;
import dev.ftb.mods.ftblibrary.icon.webp.WebPPlayer;
import dev.ftb.mods.ftblibrary.icon.webp.WebPTexture;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Displays a (possibly animated) WebP image and, while the mouse is over it, a small overlay with play/pause and
 * restart controls. Playback state is per-widget and runtime-only. Clicks are only consumed when they land on the
 * overlay bar, so click handling by the owner of this widget is otherwise unaffected.
 * <p>
 * Playback pauses automatically while the widget isn't being drawn, e.g. when scrolled out of view in a panel.
 * <p>
 * Panels are often rebuilt without {@link #onClosed()} being called on the discarded widgets, so a widget which
 * hasn't been drawn for {@link #IDLE_RELEASE_NANOS} is released by {@link #releaseIdle()} (called every client tick).
 * It simply restarts from the beginning if it is drawn again.
 */
public class AnimatedImageWidget extends Widget {
    private static final int BAR_HEIGHT = 14;
    private static final int BUTTON_SIZE = 14;
    private static final Color4I BAR_COLOR = Color4I.rgb(0x000000).withAlpha(140);
    private static final Color4I GLYPH_COLOR = Color4I.WHITE;
    private static final Color4I PROGRESS_COLOR = Color4I.rgb(0x55AAFF);
    private static final long IDLE_RELEASE_NANOS = 15_000_000_000L;
    private static final Set<AnimatedImageWidget> LIVE = new HashSet<>();

    private final WebPIcon icon;
    private @Nullable WebPAnimation animation;
    private @Nullable WebPPlayer player;
    private @Nullable WebPTexture texture;
    private @Nullable WebPFrameData uploadedFrame;
    private long lastDrawNanos;

    public AnimatedImageWidget(Panel panel, WebPIcon icon) {
        super(panel);
        this.icon = icon;
    }

    public WebPIcon getIcon() {
        return icon;
    }

    /**
     * @return this widget's player, or null if the image is still loading or isn't animated
     */
    public @Nullable WebPPlayer getPlayer() {
        return player;
    }

    private boolean hasControls() {
        return player != null && width >= BUTTON_SIZE * 2 + 4 && height >= BAR_HEIGHT * 2;
    }

    private void ensurePlayer(WebPAnimation anim) {
        if (anim != animation) {
            release();
            animation = anim;
            player = WebPPlayer.forAnimation(anim);
            texture = new WebPTexture(anim.getWidth(), anim.getHeight());
            LIVE.add(this);
        }
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        WebPAnimation anim = icon.getCacheEntry().getAnimation();
        if (anim == null || !anim.isAnimated()) {
            icon.draw(graphics, x, y, w, h);
            return;
        }

        ensurePlayer(anim);
        lastDrawNanos = System.nanoTime();
        WebPFrameData frame = player.update(lastDrawNanos);
        if (frame == null) {
            // first frame still being decoded: show the static image meanwhile
            icon.draw(graphics, x, y, w, h);
            return;
        }

        if (frame != uploadedFrame) {
            texture.uploadFrame(frame.argb());
            uploadedFrame = frame;
        }
        RenderSystem.setShaderTexture(0, texture.getId());
        GuiHelper.drawTexturedRect(graphics, x, y, w, h, icon.color, icon.minU, icon.minV, icon.maxU, icon.maxV);

        if (isMouseOver() && hasControls()) {
            drawControls(graphics, x, y, w, h);
        }
    }

    private void drawControls(GuiGraphics graphics, int x, int y, int w, int h) {
        int by = y + h - BAR_HEIGHT;
        BAR_COLOR.draw(graphics, x, by, w, BAR_HEIGHT);

        // play/pause glyph
        int gx = x + 4;
        int gy = by + 3;
        if (player.isPlaying()) {
            GLYPH_COLOR.draw(graphics, gx + 1, gy, 3, 8);
            GLYPH_COLOR.draw(graphics, gx + 6, gy, 3, 8);
        } else {
            for (int i = 0; i < 4; i++) {
                GLYPH_COLOR.draw(graphics, gx + 1 + i * 2, gy + i, 2, 8 - i * 2);
            }
        }

        Icons.REFRESH.draw(graphics, x + BUTTON_SIZE + 2, by + 1, 12, 12);

        PROGRESS_COLOR.draw(graphics, x, y + h - 2, Math.round(w * player.getProgress()), 2);
    }

    private boolean isOverBar() {
        return getMouseY() >= getY() + height - BAR_HEIGHT && getMouseY() < getY() + height;
    }

    /**
     * @return true if the mouse is currently over the play/pause/restart overlay bar (which is only shown on hover)
     */
    public boolean isMouseOverControls() {
        return isMouseOver() && hasControls() && isOverBar();
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (isMouseOverControls()) {
            if (button.isLeft()) {
                int rel = getMouseX() - getX();
                if (rel < BUTTON_SIZE) {
                    player.toggle();
                    playClickSound();
                } else if (rel < BUTTON_SIZE * 2) {
                    player.restart();
                    playClickSound();
                }
            }
            // swallow all clicks on the bar so they don't trigger the image's own click handling
            return true;
        }
        return false;
    }

    @Override
    public void onClosed() {
        release();
        super.onClosed();
    }

    /**
     * Free the decoder thread and GPU texture. Safe to call repeatedly; playback restarts if drawn again.
     */
    public void release() {
        if (player != null) {
            player.close();
            player = null;
        }
        if (texture != null) {
            texture.close();
            texture = null;
        }
        animation = null;
        uploadedFrame = null;
        LIVE.remove(this);
    }

    /**
     * Release any widgets which have stopped being drawn. Call on the render thread, e.g. each client tick.
     */
    public static void releaseIdle() {
        if (LIVE.isEmpty()) {
            return;
        }
        long now = System.nanoTime();
        for (var widget : new ArrayList<>(LIVE)) {
            if (now - widget.lastDrawNanos > IDLE_RELEASE_NANOS) {
                widget.release();
            }
        }
    }
}
