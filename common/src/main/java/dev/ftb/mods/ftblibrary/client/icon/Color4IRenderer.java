package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public enum Color4IRenderer implements IconRenderer<Color4I> {
    INSTANCE;

    @Override
    public void render(Color4I icon, GuiGraphics graphics, int x, int y, int w, int h) {
        if (w > 0 && h > 0 && !icon.isEmpty()) {
            graphics.fill(RenderPipelines.GUI, x, y, x + w, y + h, icon.rgba());
        }
    }

    @Override
    public boolean hasPixelBuffer(Color4I icon) {
        return true;
    }

    @Override
    @Nullable
    public PixelBuffer createPixelBuffer(Color4I icon) {
        return icon.isEmpty() ?
                null :
                Util.make(new PixelBuffer(1, 1), b -> b.setRGB(0, 0, icon.rgba()));
    }
}
