package dev.ftb.mods.ftblibrary.ui.layout;

/**
 * How a widget's size along one axis is decided by a layout that understands {@link LayoutHints}.
 */
public enum Flex {
    /** Keep the size the widget already has (set by its constructor or by the caller). */
    INTRINSIC,
    /** Take a share of whatever space the siblings leave over, or fill the cross axis. */
    RESIZE
}
