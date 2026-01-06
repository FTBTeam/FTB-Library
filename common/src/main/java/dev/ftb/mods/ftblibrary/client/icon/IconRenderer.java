package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.AnimatedIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.client.gui.GuiGraphics;
import org.jspecify.annotations.Nullable;

public interface IconRenderer<T extends Icon<T>> {
    /**
     * The default render method for this icon type.
     *
     * @param icon the icon being rendered
     * @param graphics graphics context
     * @param x the X position
     * @param y the Y position
     * @param w the icon width
     * @param h the icon height
     */
    void render(T icon, GuiGraphics graphics, int x, int y, int w, int h);

    /**
     * In most cases, this can just default to {@link #render(Icon, GuiGraphics, int, int, int, int)}, but some icon types
     * can have dynamic textures which change over time. This method can be overridden for those types to draw a
     * static, unchanging image, where needed.
     *
     * @param icon the icon being rendered
     * @param graphics graphics context
     * @param x the X position
     * @param y the Y position
     * @param w the icon width
     * @param h the icon height
     */
    default void renderStatic(T icon, GuiGraphics graphics, int x, int y, int w, int h) {
        render(icon, graphics, x, y, w, h);
    }

    /**
     * Does this icon have a {@link PixelBuffer} ?
     *
     * @return true if the icon has a pixel buffer
     */
    default boolean hasPixelBuffer(T icon) {
        return false;
    }

    /**
     * @return null if this icon does not have a pixel buffer, or if it failed to load
     */
    @Nullable
    default PixelBuffer createPixelBuffer(T icon) {
        return null;
    }

    /**
     * The number of animation frames in a pixel-buffer icon. Note: not to be confused with {@link AnimatedIcon},
     * which is a collection of individual icons. This returns 1 for most icon types, but icons with animated
     * textures (currently only atlas sprite icons) may have multiple frames.
     *
     * @return the number of frames
     */
    default int getPixelBufferFrameCount() {
        return 1;
    }

    /**
     * Get the aspect ratio of the icon, which is the width divided by height. For most icon types this is always 1.0,
     * since icons do not in general know what size they are (they're scaled when drawn). However, for atlas sprite
     * and image icons, the underlying image's aspect ratio is returned.
     *
     * @return the aspect ratio of the icon
     */
    default double aspectRatio(T icon) {
        return 1.0;
    }

//    default void draw3D(GuiGraphics graphics) {
//        graphics.pose().pushMatrix();
//        graphics.pose().scale(1F / 16F, 1F / 16F);
//        draw(graphics, -8, -8, 16, 16);
//        graphics.pose().popMatrix();
//    }
}
