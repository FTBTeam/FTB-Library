package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Lays a panel's widgets out in a single row or column. Unlike {@link WidgetLayout.Vertical} and
 * {@link WidgetLayout.Horizontal}, it can also place widgets on the cross axis (the x axis for a column, the y axis
 * for a row) and optionally stretch them to fill it.
 * <p>
 * Instances are immutable and hold no per-layout state, so they are safe to share. Create them with
 * {@link WidgetLayout#vertical()} or {@link WidgetLayout#horizontal()}:
 * <pre>{@code
 * align(WidgetLayout.vertical().padding(Insets.of(2, 4)).spacing(2).fillCross().build());
 * }</pre>
 * <p>
 * The <b>main axis</b> behaves exactly like the older layouts: widgets are placed one after another, separated by
 * the spacing, and {@link #align(Panel)} returns the total extent including the leading and trailing insets.
 * <p>
 * The <b>cross axis</b> is left untouched unless at least one cross-axis option is set (a non-zero cross inset, a
 * cross alignment other than {@link Align#START}, {@link Builder#fillCross()}, or an explicit
 * {@link Builder#crossSize(int)}). This keeps the default builder output identical to the older layouts.
 * When the cross axis is managed, the space the widgets are placed in is, in order of preference:
 * <ol>
 *     <li>the explicit {@code crossSize}, if one was given;</li>
 *     <li>the panel's current cross size minus the cross insets, if the panel already has a size;</li>
 *     <li>the widest widget, otherwise.</li>
 * </ol>
 * The layout never resizes the panel itself. Use {@link #measureCross(Panel)} to find the size the panel would need.
 * <p>
 * <b>Hints.</b> Widgets can carry {@link LayoutHints}; without them nothing below applies.
 * <ul>
 *     <li>A widget with {@link Flex#RESIZE} on the main axis takes a share of the panel's main size left over by the
 *     other widgets, insets, margins and spacing. Shares follow the widgets' weights, are clamped to their min/max
 *     size, and whole pixels are handed out exactly. If the panel has no main size yet, or the layout is
 *     {@link Builder#unboundedMain()}, such widgets keep their current size (clamped to their bounds). If the fixed
 *     content already overflows the panel, resizing widgets shrink to their minimum and the rest overflows.</li>
 *     <li>A widget with {@link Flex#RESIZE} on the cross axis fills it, like {@link Builder#fillCross()} for just that
 *     widget. Its own {@code hAlign}/{@code vAlign} replaces the layout's cross alignment.</li>
 *     <li>Margins add space around a widget on both axes. Alignment hints along the main axis are ignored.</li>
 *     <li>Widgets that are not resizing keep their size; min/max only bound sizes this layout assigns.</li>
 *     <li>A panel that {@link Panel#setFitContent(boolean, boolean) fits its content} on an axis is never stretched
 *     along that axis, whatever its hints or the {@link Builder#fillCross()} option say.</li>
 * </ul>
 * Hints are read when the layout runs, so a panel using them should be laid out after its widgets are configured, and
 * again if the panel's size changes (see {@link Panel#getLayout()}).
 */
public final class LinearLayout implements WidgetLayout {
    private final boolean vertical;
    private final Insets insets;
    private final int spacing;
    private final Align crossAlign;
    private final boolean fillCross;
    private final int crossSize;
    private final boolean unboundedMain;

    private LinearLayout(boolean vertical, Insets insets, int spacing, Align crossAlign, boolean fillCross, int crossSize, boolean unboundedMain) {
        this.vertical = vertical;
        this.insets = insets;
        this.spacing = spacing;
        this.crossAlign = crossAlign;
        this.fillCross = fillCross;
        this.crossSize = crossSize;
        this.unboundedMain = unboundedMain;
    }

    public boolean isVertical() {
        return vertical;
    }

    public Insets getInsets() {
        return insets;
    }

    public int getSpacing() {
        return spacing;
    }

    public Align getCrossAlign() {
        return crossAlign;
    }

    public boolean isFillCross() {
        return fillCross;
    }

    @Override
    public int align(Panel panel) {
        return align(panel, LayoutMath.available(panel.getWidth()), LayoutMath.available(panel.getHeight()));
    }

    @Override
    public int align(Panel panel, int availableWidth, int availableHeight) {
        int availableMain = vertical ? availableHeight : availableWidth;
        int availableCross = vertical ? availableWidth : availableHeight;
        List<Widget> widgets = panel.getWidgets();
        int n = widgets.size();

        LayoutHints[] hints = new LayoutHints[n];
        for (int i = 0; i < n; i++) {
            hints[i] = widgets.get(i).getLayoutHints();
        }

        int mainStart = vertical ? insets.top() : insets.left();
        int mainEnd = vertical ? insets.bottom() : insets.right();

        // Main axis: decide sizes (only RESIZE children can change), then stack the margin-inclusive footprints
        int[] mainSizes = resolveMainSizes(widgets, hints, availableMain);
        int[] footprints = new int[n];
        for (int i = 0; i < n; i++) {
            footprints[i] = mainSizes[i] + mainMargin(hints[i], true) + mainMargin(hints[i], false);
        }
        int[] mainOffsets = LayoutMath.stackOffsets(footprints, mainStart, spacing);

        boolean manageCross = managesCross(hints);
        int crossStart = vertical ? insets.left() : insets.top();
        int available = manageCross ? crossAvailable(availableCross, widgets, hints) : 0;

        for (int i = 0; i < n; i++) {
            Widget w = widgets.get(i);
            LayoutHints h = hints[i];

            if (mainSizes[i] != (vertical ? w.getHeight() : w.getWidth())) {
                if (vertical) {
                    w.setHeight(mainSizes[i]);
                } else {
                    w.setWidth(mainSizes[i]);
                }
            }
            int mainPos = mainOffsets[i] + mainMargin(h, true);
            if (vertical) {
                w.setY(mainPos);
            } else {
                w.setX(mainPos);
            }

            if (manageCross) {
                int leadMargin = crossMargin(h, true);
                int crossMargins = leadMargin + crossMargin(h, false);

                if ((fillCross || crossFlex(h) == Flex.RESIZE) && !w.isFittingContent(vertical)) {
                    int lo = vertical ? h.minWidth() : h.minHeight();
                    int hi = vertical ? h.maxWidth() : h.maxHeight();
                    int size = Math.clamp(available - crossMargins, lo, hi);
                    if (vertical) {
                        w.setWidth(size);
                    } else {
                        w.setHeight(size);
                    }
                }
                // read the size back: setWidth/setHeight may be overridden or clamp the value
                int size = vertical ? w.getWidth() : w.getHeight();

                Align align = crossAlignOf(h);
                int pos = crossStart + leadMargin + LayoutMath.alignOffset(align, available - crossMargins, size);
                if (vertical) {
                    w.setX(pos);
                } else {
                    w.setY(pos);
                }
            }
        }

        return LayoutMath.stackEnd(footprints, mainStart, spacing) + mainEnd;
    }

    /**
     * Sizes along the main axis. Anything not marked {@link Flex#RESIZE} keeps its current size. Resizing children
     * share what is left of the available main size, unless there is no limit ({@link Size#UNBOUNDED}) or the layout
     * was built with {@link Builder#unboundedMain()}; then they keep their current size, clamped to their bounds.
     */
    private int[] resolveMainSizes(List<Widget> widgets, LayoutHints[] hints, int availableMain) {
        int n = widgets.size();
        int[] sizes = new int[n];
        int[] resizing = new int[n];
        int resizeCount = 0;
        long fixed = 0;

        for (int i = 0; i < n; i++) {
            Widget w = widgets.get(i);
            sizes[i] = vertical ? w.getHeight() : w.getWidth();
            if (mainFlex(hints[i]) == Flex.RESIZE && !w.isFittingContent(!vertical)) {
                resizing[resizeCount++] = i;
                fixed += mainMargin(hints[i], true) + mainMargin(hints[i], false);
            } else {
                fixed += sizes[i] + mainMargin(hints[i], true) + mainMargin(hints[i], false);
            }
        }
        if (resizeCount == 0) {
            return sizes;
        }

        boolean bounded = !unboundedMain && availableMain >= 0;

        int[] weights = new int[resizeCount];
        int[] mins = new int[resizeCount];
        int[] maxs = new int[resizeCount];
        for (int k = 0; k < resizeCount; k++) {
            LayoutHints h = hints[resizing[k]];
            weights[k] = h.weight();
            mins[k] = vertical ? h.minHeight() : h.minWidth();
            maxs[k] = vertical ? h.maxHeight() : h.maxWidth();
        }

        if (bounded) {
            long mainInsets = vertical ? insets.vertical() : insets.horizontal();
            long gaps = (long) spacing * (n - 1);
            long remaining = availableMain - mainInsets - gaps - fixed;
            int[] shares = LayoutMath.distribute(Math.clamp(remaining, 0, Integer.MAX_VALUE), weights, mins, maxs);
            for (int k = 0; k < resizeCount; k++) {
                sizes[resizing[k]] = shares[k];
            }
        } else {
            for (int k = 0; k < resizeCount; k++) {
                sizes[resizing[k]] = Math.clamp(sizes[resizing[k]], mins[k], maxs[k]);
            }
        }
        return sizes;
    }

    private Flex mainFlex(LayoutHints h) {
        return vertical ? h.vFlex() : h.hFlex();
    }

    private Flex crossFlex(LayoutHints h) {
        return vertical ? h.hFlex() : h.vFlex();
    }

    private Align crossAlignOf(LayoutHints h) {
        Align own = vertical ? h.hAlign() : h.vAlign();
        return own != null ? own : crossAlign;
    }

    private static int mainMargin(LayoutHints h, boolean leading, boolean vertical) {
        Insets m = h.margin();
        return vertical ? (leading ? m.top() : m.bottom()) : (leading ? m.left() : m.right());
    }

    private int mainMargin(LayoutHints h, boolean leading) {
        return mainMargin(h, leading, vertical);
    }

    private int crossMargin(LayoutHints h, boolean leading) {
        return mainMargin(h, leading, !vertical);
    }

    /**
     * The size this layout wants for the panel's widgets: the stacked main-axis extent, including insets, margins and
     * spacing, by the cross-axis size from {@link #measureCross(Panel)} (or the fixed {@link Builder#crossSize(int)}
     * plus cross insets). Widgets marked {@link Flex#RESIZE} count at their current size, clamped to their bounds,
     * because the room they would fill is not part of what the content wants. The available size is ignored.
     */
    @Override
    public Size measure(Panel panel, int availableWidth, int availableHeight) {
        List<Widget> widgets = panel.getWidgets();
        int n = widgets.size();
        LayoutHints[] hints = new LayoutHints[n];
        for (int i = 0; i < n; i++) {
            hints[i] = widgets.get(i).getLayoutHints();
        }

        int[] mainSizes = resolveMainSizes(widgets, hints, Size.UNBOUNDED);
        int[] footprints = new int[n];
        for (int i = 0; i < n; i++) {
            footprints[i] = mainSizes[i] + mainMargin(hints[i], true) + mainMargin(hints[i], false);
        }
        int mainStart = vertical ? insets.top() : insets.left();
        int mainEnd = vertical ? insets.bottom() : insets.right();
        int main = LayoutMath.stackEnd(footprints, mainStart, spacing) + mainEnd;

        int crossInsets = vertical ? insets.horizontal() : insets.vertical();
        int cross = crossSize > 0 ? crossSize + crossInsets : measureCross(panel);

        return vertical ? new Size(cross, main) : new Size(main, cross);
    }

    /**
     * The cross-axis size a panel needs to hold its widgets: the widest (or tallest) widget, including its margins, plus the cross insets.
     * Useful for panels that size themselves to their content before filling.
     */
    public int measureCross(Panel panel) {
        List<Widget> widgets = panel.getWidgets();
        int[] sizes = new int[widgets.size()];
        for (int i = 0; i < sizes.length; i++) {
            Widget w = widgets.get(i);
            LayoutHints h = w.getLayoutHints();
            sizes[i] = (vertical ? w.getWidth() : w.getHeight()) + crossMargin(h, true) + crossMargin(h, false);
        }
        return LayoutMath.max(sizes) + (vertical ? insets.horizontal() : insets.vertical());
    }

    @Override
    @NotNull
    public Padding getLayoutPadding() {
        return new Padding(insets.vertical(), insets.horizontal());
    }

    private boolean managesCross(LayoutHints[] hints) {
        int crossInsets = vertical ? insets.horizontal() : insets.vertical();
        if (fillCross || crossAlign != Align.START || crossInsets != 0 || crossSize > 0) {
            return true;
        }
        for (LayoutHints h : hints) {
            if ((vertical ? h.margin().horizontal() : h.margin().vertical()) != 0) {
                return true;
            }
            if (crossFlex(h) == Flex.RESIZE || (vertical ? h.hAlign() : h.vAlign()) != null) {
                return true;
            }
        }
        return false;
    }

    private int crossAvailable(int availableCross, List<Widget> widgets, LayoutHints[] hints) {
        int crossInsets = vertical ? insets.horizontal() : insets.vertical();
        if (crossSize > 0) {
            return crossSize;
        }
        if (availableCross >= 0) {
            return Math.max(0, availableCross - crossInsets);
        }
        int widest = 0;
        for (int i = 0; i < widgets.size(); i++) {
            int size = vertical ? widgets.get(i).getWidth() : widgets.get(i).getHeight();
            widest = Math.max(widest, size + crossMargin(hints[i], true) + crossMargin(hints[i], false));
        }
        return widest;
    }

    /**
     * Start a new column layout.
     *
     * @see WidgetLayout#vertical()
     */
    public static Builder vertical() {
        return new Builder(true);
    }

    /**
     * Start a new row layout.
     *
     * @see WidgetLayout#horizontal()
     */
    public static Builder horizontal() {
        return new Builder(false);
    }

    public static final class Builder {
        private final boolean vertical;
        private Insets insets = Insets.ZERO;
        private int spacing = 0;
        private Align crossAlign = Align.START;
        private boolean fillCross = false;
        private int crossSize = 0;
        private boolean unboundedMain = false;

        private Builder(boolean vertical) {
            this.vertical = vertical;
        }

        /** Space around the content. For a column, top and bottom are the main axis; for a row, left and right. */
        public Builder padding(Insets insets) {
            this.insets = Objects.requireNonNull(insets);
            return this;
        }

        /** Same padding on all four sides. */
        public Builder padding(int all) {
            return padding(Insets.of(all));
        }

        /** Gap between consecutive widgets. */
        public Builder spacing(int spacing) {
            this.spacing = spacing;
            return this;
        }

        /** Where widgets sit on the cross axis. Has no visible effect together with {@link #fillCross()}. */
        public Builder crossAlign(Align align) {
            this.crossAlign = Objects.requireNonNull(align);
            return this;
        }

        /** Stretch every widget to the full cross-axis space (width for a column, height for a row). */
        public Builder fillCross() {
            this.fillCross = true;
            return this;
        }

        /**
         * Fix the cross-axis space widgets are placed in (excluding insets), instead of using the panel's size or the
         * widest widget. Values of zero or less mean "not set".
         */
        public Builder crossSize(int size) {
            this.crossSize = size;
            return this;
        }

        /**
         * Treat the main axis as having unlimited room, so {@link Flex#RESIZE} children keep their current size
         * instead of dividing the panel's size. Use this for panels that scroll along the layout axis.
         */
        public Builder unboundedMain() {
            this.unboundedMain = true;
            return this;
        }

        public LinearLayout build() {
            return new LinearLayout(vertical, insets, spacing, crossAlign, fillCross, crossSize, unboundedMain);
        }
    }
}
