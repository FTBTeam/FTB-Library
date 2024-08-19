package dev.ftb.mods.ftblibrary.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.util.UndashedUuid;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.io.File;
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

    @Override
    @Environment(EnvType.CLIENT)
    public void bindTexture() {
        var manager = Minecraft.getInstance().getTextureManager();
        var img = manager.getTexture(texture);

        if (img == null) {
            if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
                img = new HttpTexture(null, url, MISSING_IMAGE, false, null);
            } else {
                File file = null;

                if (uri.getScheme().equals("file")) {
                    try {
                        file = new File(uri.getPath());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (file == null) {
                    file = new File(uri);
                }

                img = new HttpTexture(file, url, MISSING_IMAGE, false, null);
            }

            manager.register(texture, img);
        }

        RenderSystem.bindTexture(img.getId());
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
