package dev.ftb.mods.ftblibrary.client.gui.widget;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.icon.WebPIconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.webp.WebPCache;
import dev.ftb.mods.ftblibrary.client.icon.webp.WebPTexture;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.WebPIcon;
import dev.ftb.mods.ftblibrary.icon.webp.WebPAnimation;
import dev.ftb.mods.ftblibrary.icon.webp.WebPFrameData;
import dev.ftb.mods.ftblibrary.icon.webp.WebPPlayer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

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
    private static final int BAR_COLOR = 0x8C000000;
    private static final int PROGRESS_COLOR = 0xC055AAF0;
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

    private void drawStatic(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        WebPIconRenderer.INSTANCE.render(icon, graphics, x, y, w, h);
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        WebPAnimation anim = WebPCache.get(icon.texture, icon.uri).getAnimation();
        if (anim == null || !anim.isAnimated()) {
            drawStatic(graphics, x, y, w, h);
            return;
        }

        ensurePlayer(anim);
        assert player != null && texture != null;

        lastDrawNanos = System.nanoTime();
        WebPFrameData frame = player.update(lastDrawNanos);
        if (frame == null) {
            // first frame still being decoded: show the static image meanwhile
            drawStatic(graphics, x, y, w, h);
            return;
        }

        if (frame != uploadedFrame) {
            texture.uploadFrame(frame.argb());
            uploadedFrame = frame;
        }
        graphics.guiRenderState.addGuiElement(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()),
                new Matrix3x2f(graphics.pose()),
                x, y, x + w, y + h,
                icon.minU, icon.maxU, icon.minV, icon.maxV,
                icon.color.rgba(),
                graphics.scissorStack.peek()
        ));

        if ((!player.isPlaying() || isMouseOver()) && hasControls()) {
            drawControls(graphics, x, y, w, h);
        }
    }

    private void drawControls(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        assert player != null;

        int by = y + h - BAR_HEIGHT;
        graphics.fill(x, by, x + w, by + BAR_HEIGHT, BAR_COLOR);

        // play/pause glyph
        if (player.isPlaying()) {
            IconHelper.renderIcon(Icons.PAUSE, graphics, x + 2, by + 1, 12, 12);
        } else {
            IconHelper.renderIcon(Icons.RIGHT, graphics, x + 2, by + 1, 12, 12);
        }
        IconHelper.renderIcon(Icons.REFRESH, graphics, x + BUTTON_SIZE + 2, by + 1, 12, 12);

        graphics.fill(x, y + h - 2, x + Math.round(w * player.getProgress()), y + h, PROGRESS_COLOR);
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
                assert player != null;
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
