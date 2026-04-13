package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

public class IconHelper {
    public static <T extends Icon<T>> void renderIcon(@Nullable Icon<T> icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        if (icon != null) {
            icon.getRenderer().render(icon.self(), graphics, x, y, w, h);
        }
    }

    public static <T extends Icon<T>> void renderIconStatic(@Nullable Icon<T> icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        if (icon != null) {
            icon.getRenderer().renderStatic(icon.self(), graphics, x, y, w, h);
        }
    }

    public static <T extends Icon<T>> boolean hasPixelBuffer(@Nullable Icon<T> icon) {
        return icon != null && icon.getRenderer().hasPixelBuffer(icon.self());
    }

    @Nullable
    public static <T extends Icon<T>> PixelBuffer createPixelBuffer(@Nullable Icon<T> icon) {
        return icon != null ? icon.getRenderer().createPixelBuffer(icon.self()) : null;
    }

    public static <T extends Icon<T>> double aspectRatio(@Nullable Icon<T> icon) {
        return icon != null ? icon.getRenderer().aspectRatio(icon.self()) : 1;
    }

    public static <T extends Icon<T>> int getPixelBufferFrameCount(@Nullable Icon<T> icon) {
        return icon != null ? icon.getRenderer().getPixelBufferFrameCount(icon.self()) : 1;
    }

}
