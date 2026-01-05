package dev.ftb.mods.ftblibrary.icon;

import com.google.common.base.Objects;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class ImageIcon extends Icon implements IResourceIcon {
    public static final Identifier MISSING_IMAGE = FTBLibrary.rl("textures/gui/missing_image.png");

    public final Identifier texture;
    public float minU, minV, maxU, maxV;
    public double tileSize;
    public Color4I color;

    public ImageIcon(Identifier tex) {
        texture = tex;
        minU = 0;
        minV = 0;
        maxU = 1;
        maxV = 1;
        tileSize = 0;
        color = Color4I.WHITE;
    }

    @Override
    public ImageIcon copy() {
        var icon = new ImageIcon(texture);
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
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.blit(texture, x, y, x + w, y + h, minU, maxU, minV, maxV);
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
        return texture.toString();
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
    public boolean hasPixelBuffer() {
        return true;
    }

    @Override
    @Nullable
    public PixelBuffer createPixelBuffer() {
        try {
            return PixelBuffer.from(Minecraft.getInstance().getResourceManager().getResource(texture).orElseThrow().open());
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public double aspectRatio() {
        if (maxV == minV) return 1.0;

        return (maxU - minU) / (maxV - minV);
    }

    @Override
    public Identifier getIdentifier() {
        return texture;
    }
}
