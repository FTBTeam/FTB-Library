package dev.ftb.mods.ftblibrary.ui.layout;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Per-widget instructions for layouts that understand them; different layouts pay attention to different subsets
 * of these hints.
 * <p>
 * This is an immutable value, so is safe to store and share between widgets; use the {@code with} methods to derive
 * a changed copy from {@link #DEFAULT}:
 * <pre>{@code
 * widget.setLayoutHints(LayoutHints.DEFAULT.withVFlex(Flex.RESIZE).withWeight(2));
 * }</pre>
 * Hints must be set before the layout runs; changing them afterward has no effect until the next layout.
 *
 * @param hFlex     how the width is decided (never null)
 * @param vFlex     how the height is decided (never null)
 * @param hAlign    horizontal placement when this widget is on the cross axis of a layout, or {@code null} to use the
 *                  layout's own cross alignment
 * @param vAlign    vertical placement when this widget is on the cross axis of a layout, or {@code null} to use the
 *                  layout's own cross alignment
 * @param weight    share of leftover space relative to other {@link Flex#RESIZE} siblings (at least 1)
 * @param minWidth  lower bound for widths a layout assigns
 * @param minHeight lower bound for heights a layout assigns
 * @param maxWidth  upper bound for widths a layout assigns ({@link #UNBOUNDED} for none)
 * @param maxHeight upper bound for heights a layout assigns ({@link #UNBOUNDED} for none)
 * @param margin    space kept clear around the widget inside its parent's layout (never null; use {@link Insets#ZERO})
 * @param colSpan   number of grid columns the widget covers (at least 1; only {@link GridLayout} uses this)
 * @param rowSpan   number of grid rows the widget covers (at least 1; only {@link GridLayout} uses this)
 */
public record LayoutHints(Flex hFlex, Flex vFlex, @Nullable Align hAlign, @Nullable Align vAlign, int weight,
                          int minWidth, int minHeight, int maxWidth, int maxHeight, Insets margin, int colSpan, int rowSpan) {
    public static final int UNBOUNDED = Integer.MAX_VALUE;

    /** What every widget has until told otherwise; makes layouts behave as they did before hints existed. */
    public static final LayoutHints DEFAULT = new LayoutHints(Flex.INTRINSIC, Flex.INTRINSIC, null, null, 1,
            0, 0, UNBOUNDED, UNBOUNDED, Insets.ZERO, 1, 1);

    public LayoutHints {
        Objects.requireNonNull(hFlex, "hFlex");
        Objects.requireNonNull(vFlex, "vFlex");
        Objects.requireNonNull(margin, "margin");
        weight = Math.max(1, weight);
        minWidth = Math.max(0, minWidth);
        minHeight = Math.max(0, minHeight);
        maxWidth = Math.max(minWidth, maxWidth);
        maxHeight = Math.max(minHeight, maxHeight);
        colSpan = Math.max(1, colSpan);
        rowSpan = Math.max(1, rowSpan);
    }

    public LayoutHints withHFlex(Flex flex) {
        return new LayoutHints(flex, vFlex, hAlign, vAlign, weight, minWidth, minHeight, maxWidth, maxHeight, margin, colSpan, rowSpan);
    }

    public LayoutHints withVFlex(Flex flex) {
        return new LayoutHints(hFlex, flex, hAlign, vAlign, weight, minWidth, minHeight, maxWidth, maxHeight, margin, colSpan, rowSpan);
    }

    /** Resize on both axes. */
    public LayoutHints resize() {
        return withHFlex(Flex.RESIZE).withVFlex(Flex.RESIZE);
    }

    public LayoutHints withHAlign(@Nullable Align align) {
        return new LayoutHints(hFlex, vFlex, align, vAlign, weight, minWidth, minHeight, maxWidth, maxHeight, margin, colSpan, rowSpan);
    }

    public LayoutHints withVAlign(@Nullable Align align) {
        return new LayoutHints(hFlex, vFlex, hAlign, align, weight, minWidth, minHeight, maxWidth, maxHeight, margin, colSpan, rowSpan);
    }

    public LayoutHints withWeight(int weight) {
        return new LayoutHints(hFlex, vFlex, hAlign, vAlign, weight, minWidth, minHeight, maxWidth, maxHeight, margin, colSpan, rowSpan);
    }

    public LayoutHints withMinSize(int width, int height) {
        return new LayoutHints(hFlex, vFlex, hAlign, vAlign, weight, width, height, maxWidth, maxHeight, margin, colSpan, rowSpan);
    }

    public LayoutHints withMaxSize(int width, int height) {
        return new LayoutHints(hFlex, vFlex, hAlign, vAlign, weight, minWidth, minHeight, width, height, margin, colSpan, rowSpan);
    }

    public LayoutHints withMargin(Insets margin) {
        return new LayoutHints(hFlex, vFlex, hAlign, vAlign, weight, minWidth, minHeight, maxWidth, maxHeight, margin, colSpan, rowSpan);
    }

    /** How many grid columns and rows the widget covers; ignored by layouts other than {@link GridLayout}. */
    public LayoutHints withSpan(int columns, int rows) {
        return new LayoutHints(hFlex, vFlex, hAlign, vAlign, weight, minWidth, minHeight, maxWidth, maxHeight, margin, columns, rows);
    }
}
