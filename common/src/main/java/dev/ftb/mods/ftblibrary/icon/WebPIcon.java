package dev.ftb.mods.ftblibrary.icon;

import dev.ftb.mods.ftblibrary.icon.webp.WebPAnimation;
import dev.ftb.mods.ftblibrary.icon.webp.WebPCache;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

/**
 * A WebP image, loaded from a resource pack or a URI. As an {@link Icon} it is stateless and always draws the
 * <em>first frame</em> only; use {@link dev.ftb.mods.ftblibrary.ui.AnimatedImageWidget} to play animations.
 */
public class WebPIcon extends ImageIcon {
    public final @Nullable URI uri;

    public WebPIcon(ResourceLocation tex) {
        super(tex);
        this.uri = null;
    }

    public WebPIcon(URI uri) {
        this(ResourceLocation.parse("remote_image:webp_" + UUID.nameUUIDFromBytes(uri.toString().getBytes(StandardCharsets.UTF_8)).toString().replace("-", "")), uri);
    }

    public WebPIcon(ResourceLocation tex, @Nullable URI uri) {
        super(tex);
        this.uri = uri;
    }

    public static boolean isWebPId(String id) {
        return id.toLowerCase(Locale.ROOT).endsWith(".webp");
    }

    @Override
    public WebPIcon copy() {
        var icon = new WebPIcon(texture, uri);
        icon.minU = minU;
        icon.minV = minV;
        icon.maxU = maxU;
        icon.maxV = maxV;
        icon.tileSize = tileSize;
        icon.color = color;
        return icon;
    }

    /**
     * @return the shared cache entry for this image; loading starts on first call
     */
    public WebPCache.Entry getCacheEntry() {
        return WebPCache.get(texture, uri);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void bindTexture() {
        var entry = getCacheEntry();
        var tex = entry.isFailed() ? null : entry.getFirstFrameTexture(texture);
        var id = tex != null ? tex : MISSING_IMAGE;
        RenderSystem.setShaderTexture(0, Minecraft.getInstance().getTextureManager().getTexture(id).getId());
    }

    @Override
    public double aspectRatio() {
        WebPAnimation anim = getCacheEntry().getAnimation();
        return anim == null ? super.aspectRatio() : (double) anim.getWidth() / anim.getHeight();
    }

    @Override
    @Nullable
    public PixelBuffer createPixelBuffer() {
        WebPAnimation anim = getCacheEntry().getAnimation();
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
    public String toString() {
        return uri != null ? uri.toString() : texture.toString();
    }
}
