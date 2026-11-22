package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Lays widgets out left to right and wraps to a new line when the next one would not fit, like text. It suits
 * collections of buttons, icons or tags whose count is not known in advance.
 * <p>
 * The room on a line is the panel's width minus the insets. If the panel has no width yet, nothing wraps. A widget
 * wider than a whole line gets a line to itself. Widgets keep their size ({@link Flex} hints are ignored) but their
 * margins are respected.
 * <p>
 * Each line is positioned horizontally within the available width by {@link Builder#lineAlign}, and widgets of
 * different heights are positioned vertically within their line by {@link Builder#itemAlign}.
 * <p>
 * {@link #align(Panel)} returns the total height including the vertical insets.
 */
public final class FlowLayout implements WidgetLayout {
    private final Insets insets;
    private final int hSpacing;
    private final int vSpacing;
    private final Align lineAlign;
    private final Align itemAlign;

    private FlowLayout(Insets insets, int hSpacing, int vSpacing, Align lineAlign, Align itemAlign) {
        this.insets = insets;
        this.hSpacing = hSpacing;
        this.vSpacing = vSpacing;
        this.lineAlign = lineAlign;
        this.itemAlign = itemAlign;
    }

    /**
     * How a row of widgets breaks into lines.
     *
     * @param lineOf     the line each widget is on
     * @param lineWidth  width of each line, including spacing between its widgets
     * @param lineHeight height of the tallest widget on each line
     */
    record Lines(int[] lineOf, int[] lineWidth, int[] lineHeight) {
        /**
         * @param widths    footprint width of each widget
         * @param heights   footprint height of each widget
         * @param available room on a line, or zero or less for no limit
         * @param spacing   gap between widgets on a line
         */
        static Lines compute(int[] widths, int[] heights, int available, int spacing) {
            int n = widths.length;
            int[] lineOf = LayoutMath.flowLines(widths, available, spacing);
            int lines = n == 0 ? 0 : lineOf[n - 1] + 1;

            int[] lineWidth = new int[lines];
            int[] lineHeight = new int[lines];
            int[] count = new int[lines];
            for (int i = 0; i < n; i++) {
                int l = lineOf[i];
                lineWidth[l] += widths[i] + (count[l] > 0 ? spacing : 0);
                lineHeight[l] = Math.max(lineHeight[l], heights[i]);
                count[l]++;
            }
            return new Lines(lineOf, lineWidth, lineHeight);
        }
    }

    /** Room on one line for a given available width: zero means no limit; otherwise at least one pixel. */
    private int lineRoom(int availableWidth) {
        return availableWidth < 0 ? 0 : Math.max(1, availableWidth - insets.horizontal());
    }

    private int[][] footprints(List<Widget> widgets) {
        int n = widgets.size();
        int[] widths = new int[n];
        int[] heights = new int[n];
        for (int i = 0; i < n; i++) {
            Widget w = widgets.get(i);
            Insets m = w.getLayoutHints().margin();
            widths[i] = w.getWidth() + m.horizontal();
            heights[i] = w.getHeight() + m.vertical();
        }
        return new int[][]{widths, heights};
    }

    @Override
    public int align(Panel panel) {
        return align(panel, LayoutMath.available(panel.getWidth()), LayoutMath.available(panel.getHeight()));
    }

    @Override
    public int align(Panel panel, int availableWidth, int availableHeight) {
        List<Widget> widgets = panel.getWidgets();
        int n = widgets.size();

        int[][] foot = footprints(widgets);
        int[] widths = foot[0];
        int[] heights = foot[1];

        int available = lineRoom(availableWidth);
        Lines solved = Lines.compute(widths, heights, available, hSpacing);
        int[] lineOf = solved.lineOf();
        int[] lineWidth = solved.lineWidth();
        int[] lineHeight = solved.lineHeight();
        int lines = lineHeight.length;

        int rowArea = available > 0 ? available : LayoutMath.max(lineWidth);
        int[] lineTop = LayoutMath.stackOffsets(lineHeight, insets.top(), vSpacing);
        int[] cursor = new int[lines];
        for (int l = 0; l < lines; l++) {
            cursor[l] = insets.left() + LayoutMath.alignOffset(lineAlign, rowArea, lineWidth[l]);
        }

        for (int i = 0; i < n; i++) {
            Widget w = widgets.get(i);
            Insets m = w.getLayoutHints().margin();
            int l = lineOf[i];

            w.setX(cursor[l] + m.left());
            w.setY(lineTop[l] + m.top() + LayoutMath.alignOffset(itemAlign, lineHeight[l] - heights[i], 0));
            cursor[l] += widths[i] + hSpacing;
        }

        return LayoutMath.stackEnd(lineHeight, insets.top(), vSpacing) + insets.bottom();
    }

    /**
     * The size needed to hold the widgets once wrapped to the available width: the widest line by the stacked line
     * heights, plus insets. With no limit on the width everything sits on one line. The available height is ignored.
     * Zero or more is a real limit here (it always leaves room for one widget per line); {@link Size#UNBOUNDED}
     * means no limit. {@link #align(Panel)} treats a panel with no width as having no limit.
     */
    @Override
    public Size measure(Panel panel, int availableWidth, int availableHeight) {
        int[][] foot = footprints(panel.getWidgets());
        int available = lineRoom(availableWidth);
        Lines solved = Lines.compute(foot[0], foot[1], available, hSpacing);

        int width = LayoutMath.max(solved.lineWidth()) + insets.horizontal();
        int height = LayoutMath.stackEnd(solved.lineHeight(), insets.top(), vSpacing) + insets.bottom();
        return new Size(width, height);
    }

    @Override
    @NotNull
    public Padding getLayoutPadding() {
        return new Padding(insets.vertical(), insets.horizontal());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Insets insets = Insets.ZERO;
        private int hSpacing = 0;
        private int vSpacing = 0;
        private Align lineAlign = Align.START;
        private Align itemAlign = Align.START;

        private Builder() {
        }

        public Builder padding(Insets insets) {
            this.insets = Objects.requireNonNull(insets);
            return this;
        }

        public Builder padding(int all) {
            return padding(Insets.of(all));
        }

        /** Gap between widgets on a line, and between lines. */
        public Builder spacing(int horizontal, int vertical) {
            this.hSpacing = horizontal;
            this.vSpacing = vertical;
            return this;
        }

        public Builder spacing(int both) {
            return spacing(both, both);
        }

        /** Where each line sits within the available width: left, centred or right. */
        public Builder lineAlign(Align align) {
            this.lineAlign = Objects.requireNonNull(align);
            return this;
        }

        /** Where widgets of different heights sit within their line: top, middle or bottom. */
        public Builder itemAlign(Align align) {
            this.itemAlign = Objects.requireNonNull(align);
            return this;
        }

        public FlowLayout build() {
            return new FlowLayout(insets, hSpacing, vSpacing, lineAlign, itemAlign);
        }
    }
}
