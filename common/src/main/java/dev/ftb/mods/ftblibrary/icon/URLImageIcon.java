package dev.ftb.mods.ftblibrary.icon;

import com.mojang.util.UndashedUuid;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class URLImageIcon extends ImageIcon {
    public final URI uri;
    private final String url;

    public URLImageIcon(ResourceLocation tex, URI _uri) {
        super(tex);
        uri = _uri;
        url = uri.toString();
    }

    public URLImageIcon(URI uri) {
        this(ResourceLocation.parse("remote_image:" + UndashedUuid.toString(UUID.nameUUIDFromBytes(uri.toString().getBytes(StandardCharsets.UTF_8)))), uri);
    }

    @Override
    public URLImageIcon copy() {
        var icon = new URLImageIcon(texture, uri);
        icon.minU = minU;
        icon.minV = minV;
        icon.maxU = maxU;
        icon.maxV = maxV;
        icon.tileSize = tileSize;
        return icon;
    }

    public String toString() {
        return url;
    }

    @Override
    public PixelBuffer createPixelBuffer() {
        try (var stream = uri.toURL().openConnection(Minecraft.getInstance().getProxy()).getInputStream()) {
            return PixelBuffer.from(ImageIO.read(stream));
        } catch (Exception ex) {
            return null;
        }
    }
}
