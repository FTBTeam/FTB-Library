package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

/**
 * A width and a height in pixels, as reported by {@link WidgetLayout#measure}.
 *
 * @param width  width in pixels
 * @param height height in pixels
 */
public record Size(int width, int height) {
    /** Passed as an available width or height to mean "no limit". */
    public static final int UNBOUNDED = -1;

    public static final Size ZERO = new Size(0, 0);
}
