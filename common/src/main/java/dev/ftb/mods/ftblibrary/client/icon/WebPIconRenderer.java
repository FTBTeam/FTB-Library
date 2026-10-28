package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.client.icon.webp.WebPCache;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.icon.WebPIcon;
import dev.ftb.mods.ftblibrary.icon.webp.WebPAnimation;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

/**
 * Renders the first frame of a {@link WebPIcon}. Animated playback is handled by the AnimatedImageWidget.
 * Typed on {@link ImageIcon} because that's what {@link WebPIcon} inherits as its icon type.
 */
public enum WebPIconRenderer implements IconRenderer<ImageIcon> {
    INSTANCE;

    private static final ImageIcon MISSING = new ImageIcon(ImageIcon.MISSING_IMAGE);

    @Override
    public void render(ImageIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        if (!(icon instanceof WebPIcon webp)) {
            ImageIconRenderer.INSTANCE.render(icon, graphics, x, y, w, h);
            return;
        }

        var entry = WebPCache.get(webp.texture, webp.uri);
        // the first frame is registered under the icon's own texture id, so the standard image renderer can draw it
        if (!entry.isFailed() && entry.getFirstFrameTexture(webp.texture) != null) {
            ImageIconRenderer.INSTANCE.render(webp, graphics, x, y, w, h);
        } else {
            ImageIconRenderer.INSTANCE.render(MISSING, graphics, x, y, w, h);
        }
    }

    @Override
    public boolean hasPixelBuffer(ImageIcon icon) {
        return true;
    }

    @Override
    public @Nullable PixelBuffer createPixelBuffer(ImageIcon icon) {
        if (!(icon instanceof WebPIcon webp)) {
            return ImageIconRenderer.INSTANCE.createPixelBuffer(icon);
        }
        WebPAnimation anim = WebPCache.get(webp.texture, webp.uri).getAnimation();
        if (anim == null) {
            return null;
        }
        try {
            PixelBuffer buffer = new PixelBuffer(anim.getWidth(), anim.getHeight());
            buffer.setPixels(anim.decodeFirstFrame());
            return buffer;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public double aspectRatio(ImageIcon icon) {
        if (icon instanceof WebPIcon webp) {
            WebPAnimation anim = WebPCache.get(webp.texture, webp.uri).getAnimation();
            if (anim != null) {
                return (double) anim.getWidth() / anim.getHeight();
            }
        }
        return ImageIconRenderer.INSTANCE.aspectRatio(icon);
    }
}
