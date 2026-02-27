package dev.ftb.mods.ftblibrary.icon;

import com.google.common.base.Objects;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.ImageIconRenderer;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.net.URI;

public class ImageIcon extends Icon<ImageIcon> implements IResourceIcon {
    public static final Identifier MISSING_IMAGE = FTBLibrary.rl("textures/gui/missing_image.png");

    public final Identifier texture;
    public float minU, minV, maxU, maxV;
    public double tileSize;
    public Color4I color;
    @Nullable public final URI uri;
    @Nullable private final String url;

    public ImageIcon(Identifier texture) {
        this(texture, null);
    }

    public ImageIcon(Identifier texture, @Nullable URI uri) {
        this.texture = texture;
        minU = 0f;
        minV = 0f;
        maxU = 1f;
        maxV = 1f;
        tileSize = 0.0;
        color = Color4I.WHITE;

        this.uri = uri;
        url = uri == null ? null : uri.toString();
    }

    @Override
    public ImageIcon copy() {
        var icon = new ImageIcon(texture, uri);
        icon.minU = minU;
        icon.minV = minV;
        icon.maxU = maxU;
        icon.maxV = maxV;
        icon.tileSize = tileSize;
        return icon;
    }

    @Override
    protected void setProperties(IconProperties properties) {
        super.setProperties(properties);
        minU = (float) properties.getDouble("u0", minU);
        minV = (float) properties.getDouble("v0", minV);
        maxU = (float) properties.getDouble("u1", maxU);
        maxV = (float) properties.getDouble("v1", maxV);
        tileSize = properties.getDouble("tile_size", tileSize);
    }

    @Override
    public IconRenderer<ImageIcon> getRenderer() {
        return ImageIconRenderer.INSTANCE;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(texture, minU, minV, maxU, maxV);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ImageIcon img) {
            return texture.equals(img.texture) && minU == img.minU && minV == img.minV && maxU == img.maxU && maxV == img.maxV;
        }
        return false;
    }

    @Override
    public String toString() {
        return url == null ? texture.toString() : url;
    }

    @Override
    public ImageIcon withColor(Color4I color) {
        var icon = copy();
        icon.color = color;
        return icon;
    }

    @Override
    public ImageIcon withTint(Color4I c) {
        return withColor(color.withTint(c));
    }

    @Override
    public ImageIcon withUV(float u0, float v0, float u1, float v1) {
        var icon = copy();
        icon.minU = u0;
        icon.minV = v0;
        icon.maxU = u1;
        icon.maxV = v1;
        return icon;
    }

    @Override
    public Identifier getResourceId() {
        return texture;
    }
}
