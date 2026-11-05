package dev.ftb.mods.ftblibrary.icon;

import com.mojang.util.UndashedUuid;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.WebPIconRenderer;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

/**
 * A WebP image, loaded from a resource pack or a file URI. As an {@link Icon} it is stateless and always draws the
 * <em>first frame</em> only; use {@code AnimatedImageWidget} to play animations.
 */
public class WebPIcon extends ImageIcon {
    public WebPIcon(Identifier texture) {
        super(texture);
    }

    public WebPIcon(URI uri) {
        super(Identifier.parse("remote_image:webp_" + UndashedUuid.toString(UUID.nameUUIDFromBytes(uri.toString().getBytes(StandardCharsets.UTF_8)))), uri);
    }

    protected WebPIcon(Identifier texture, @Nullable URI uri) {
        super(texture, uri);
    }

    public static boolean isWebPId(@Nullable String id) {
        return id != null && id.toLowerCase(Locale.ROOT).endsWith(".webp");
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

    @Override
    public IconRenderer<ImageIcon> getRenderer() {
        return WebPIconRenderer.INSTANCE;
    }
}
