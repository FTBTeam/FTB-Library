package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.LazyIcon;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.client.gui.GuiGraphics;
import org.jspecify.annotations.Nullable;

public enum LazyIconRenderer implements IconRenderer<LazyIcon> {
    INSTANCE;

    @Override
    public void render(LazyIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        IconHelper.renderIcon(icon.getDelegate(), graphics, x, y, w, h);
    }

    @Override
    public void renderStatic(LazyIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        IconHelper.renderIconStatic(icon.getDelegate(), graphics, x, y, w, h);
    }

    @Override
    public boolean hasPixelBuffer(LazyIcon icon) {
        return IconHelper.hasPixelBuffer(icon.getDelegate());
    }

    @Override
    @Nullable
    public PixelBuffer createPixelBuffer(LazyIcon icon) {
        return IconHelper.createPixelBuffer(icon.getDelegate());
    }
}
