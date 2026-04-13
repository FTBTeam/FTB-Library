package dev.ftb.mods.ftblibrary.icon;

import com.mojang.util.UndashedUuid;
import net.minecraft.resources.Identifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;


public class URLImageIcon extends ImageIcon {
    public final URI uri;
    private final String url;

    public URLImageIcon(Identifier textureID, URI uri) {
        super(textureID);

        this.uri = uri;
        url = this.uri.toString();

        if (!Objects.equals(this.uri.getScheme(), "file")) {
            throw new IllegalArgumentException("Only file:// URIs are supported for URLImageIcon");
        }
    }

    public URLImageIcon(URI uri) {
        this(Identifier.parse("remote_image:" + UndashedUuid.toString(UUID.nameUUIDFromBytes(uri.toString().getBytes(StandardCharsets.UTF_8)))), uri);
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
}
