package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Lays widgets out in a grid of columns and rows. Widgets are placed in order, left to right and then top to bottom;
 * a widget with a {@link LayoutHints#withSpan(int, int) span} covers several cells and later widgets flow around it.
 * <pre>{@code
 * align(WidgetLayout.grid()
 *         .columns(GridLayout.Track.auto(), GridLayout.Track.weight(1))   // label | stretchy value
 *         .spacing(4, 2)
 *         .build());
 * }</pre>
 * <p>
 * Each column (and row) is a {@link Track}: {@link Track#auto()} fits the largest widget in it,
 * {@link Track#fixed(int)} is a set number of pixels, and {@link Track#weight(int)} shares the room left over by the
 * other tracks, but never gets smaller than its content. Rows beyond those given are auto. Weighted tracks only
 * stretch when the panel has a size in that direction; otherwise they fit their content.
 * <p>
 * Inside its cell a widget is placed by its {@link LayoutHints} alignment (falling back to the layout's
 * {@link Builder#cellAlign default}), and stretched to the cell, less its margins, on axes where it is
 * {@link Flex#RESIZE}. Margins count towards the size of auto tracks. A spanning widget makes the auto tracks it
 * covers wider if it needs more room than they already provide.
 * <p>
 * {@link #align(Panel)} returns the height needed, including vertical insets. Use {@link #measureWidth(Panel)} for
 * the width the columns need.
 */
public final class GridLayout implements WidgetLayout {
    /**
     * How one column or row of a grid is sized.
     */
    public record Track(Kind kind, int value) {
        public enum Kind {
            /** Fit the largest widget in the track. */
            AUTO,
            /** A fixed number of pixels. */
            FIXED,
            /** A share of the leftover room, in proportion to the weight. */
            WEIGHT
        }

        public Track {
            Objects.requireNonNull(kind);
            value = kind == Kind.WEIGHT ? Math.max(1, value) : Math.max(0, value);
        }

        public static Track auto() {
            return new Track(Kind.AUTO, 0);
        }

        public static Track fixed(int pixels) {
            return new Track(Kind.FIXED, pixels);
        }

        public static Track weight(int weight) {
            return new Track(Kind.WEIGHT, weight);
        }
    }

    private final Track[] columns;
    private final Track[] rows;
    private final int hGap;
    private final int vGap;
    private final Insets insets;
    private final Align cellH;
    private final Align cellV;

    private GridLayout(Track[] columns, Track[] rows, int hGap, int vGap, Insets insets, Align cellH, Align cellV) {
        this.columns = columns;
        this.rows = rows;
        this.hGap = hGap;
        this.vGap = vGap;
        this.insets = insets;
        this.cellH = cellH;
        this.cellV = cellV;
    }

    /** Everything worked out for one pass over a panel's widgets. */
    private record Solved(int[][] cells, int[] colSpan, int[] rowSpan, int[] colSizes, int[] rowSizes) {
    }

    /**
     * @param availW room for the columns, or negative for none (weighted tracks then take their content size)
     * @param availH room for the rows, or negative for none
     * @param arrange true when the widgets are about to be placed, false when only measuring. A widget that is going
     *                to be stretched along an axis with room does not hold its tracks open at the size it happens to
     *                have: that is how big it was for some earlier, larger layout, so only its minimum size counts.
     */
    private Solved solve(Panel panel, int availW, int availH, boolean arrange) {
        List<Widget> widgets = panel.getWidgets();
        int n = widgets.size();
        int cols = columns.length;

        int[] colSpan = new int[n];
        int[] rowSpan = new int[n];
        int[] footW = new int[n];
        int[] footH = new int[n];
        for (int i = 0; i < n; i++) {
            Widget w = widgets.get(i);
            LayoutHints h = w.getLayoutHints();
            colSpan[i] = Math.min(cols, h.colSpan());
            rowSpan[i] = h.rowSpan();
            boolean stretchW = arrange && availW >= 0 && h.hFlex() == Flex.RESIZE && !w.isFittingContent(true);
            boolean stretchH = arrange && availH >= 0 && h.vFlex() == Flex.RESIZE && !w.isFittingContent(false);
            footW[i] = (stretchW ? h.minWidth() : w.getWidth()) + h.margin().horizontal();
            footH[i] = (stretchH ? h.minHeight() : w.getHeight()) + h.margin().vertical();
        }

        int[][] cells = GridMath.place(cols, colSpan, rowSpan);
        int rowCount = GridMath.rowCount(cells, rowSpan);

        Track[] rowTracks = new Track[rowCount];
        for (int r = 0; r < rowCount; r++) {
            rowTracks[r] = r < rows.length ? rows[r] : Track.auto();
        }

        int[] colStart = new int[n];
        int[] rowStart = new int[n];
        for (int i = 0; i < n; i++) {
            colStart[i] = cells[i][0];
            rowStart[i] = cells[i][1];
        }

        int[] colContent = GridMath.trackContent(cols, colStart, colSpan, footW, growable(columns), hGap);
        int[] rowContent = GridMath.trackContent(rowCount, rowStart, rowSpan, footH, growable(rowTracks), vGap);

        return new Solved(cells, colSpan, rowSpan,
                GridMath.sizeTracks(columns, colContent, availW, hGap),
                GridMath.sizeTracks(rowTracks, rowContent, availH, vGap));
    }

    private static boolean[] growable(Track[] tracks) {
        boolean[] result = new boolean[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            result[i] = tracks[i].kind() != Track.Kind.FIXED;
        }
        return result;
    }

    @Override
    public int align(Panel panel) {
        return align(panel, LayoutMath.available(panel.getWidth()), LayoutMath.available(panel.getHeight()));
    }

    @Override
    public int align(Panel panel, int availableWidth, int availableHeight) {
        List<Widget> widgets = panel.getWidgets();
        int availW = availableWidth >= 0 ? Math.max(0, availableWidth - insets.horizontal()) : -1;
        int availH = availableHeight >= 0 ? Math.max(0, availableHeight - insets.vertical()) : -1;
        Solved s = solve(panel, availW, availH, true);

        int[] colOffsets = GridMath.trackOffsets(s.colSizes(), insets.left(), hGap);
        int[] rowOffsets = GridMath.trackOffsets(s.rowSizes(), insets.top(), vGap);

        for (int i = 0; i < widgets.size(); i++) {
            Widget w = widgets.get(i);
            LayoutHints h = w.getLayoutHints();
            Insets m = h.margin();
            int col = s.cells()[i][0];
            int row = s.cells()[i][1];

            int areaW = GridMath.spanSize(s.colSizes(), col, s.colSpan()[i], hGap) - m.horizontal();
            int areaH = GridMath.spanSize(s.rowSizes(), row, s.rowSpan()[i], vGap) - m.vertical();

            if (h.hFlex() == Flex.RESIZE && !w.isFittingContent(true)) {
                w.setWidth(Math.clamp(areaW, h.minWidth(), h.maxWidth()));
            }
            if (h.vFlex() == Flex.RESIZE && !w.isFittingContent(false)) {
                w.setHeight(Math.clamp(areaH, h.minHeight(), h.maxHeight()));
            }

            Align ha = h.hAlign() != null ? h.hAlign() : cellH;
            Align va = h.vAlign() != null ? h.vAlign() : cellV;
            w.setX(colOffsets[col] + m.left() + LayoutMath.alignOffset(ha, areaW, w.getWidth()));
            w.setY(rowOffsets[row] + m.top() + LayoutMath.alignOffset(va, areaH, w.getHeight()));
        }

        int rowCount = s.rowSizes().length;
        return insets.top() + insets.bottom() + Arrays.stream(s.rowSizes()).sum() + vGap * Math.max(0, rowCount - 1);
    }

    /**
     * The width the columns need: their sizes, gaps and the horizontal insets. Weighted columns count at their
     * content size when the panel has no width, and at their stretched size when it does.
     */
    public int measureWidth(Panel panel) {
        int[] sizes = solve(panel, panel.getWidth() > 0 ? Math.max(0, panel.getWidth() - insets.horizontal()) : -1, -1, false).colSizes();
        return insets.horizontal() + Arrays.stream(sizes).sum() + hGap * Math.max(0, sizes.length - 1);
    }

    /**
     * The size the tracks need to hold their widgets: auto tracks fit their content, fixed tracks keep their size, and
     * weighted tracks count at their content size because the room they would stretch into is not part of what the
     * content wants. Gaps and insets are included. The available size is ignored.
     */
    @Override
    public Size measure(Panel panel, int availableWidth, int availableHeight) {
        Solved s = solve(panel, -1, -1, false);
        int width = insets.horizontal() + Arrays.stream(s.colSizes()).sum() + hGap * Math.max(0, s.colSizes().length - 1);
        int height = insets.vertical() + Arrays.stream(s.rowSizes()).sum() + vGap * Math.max(0, s.rowSizes().length - 1);
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
        private Track[] columns = {Track.auto()};
        private Track[] rows = {};
        private int hGap = 0;
        private int vGap = 0;
        private Insets insets = Insets.ZERO;
        private Align cellH = Align.START;
        private Align cellV = Align.START;

        private Builder() {
        }

        /** Define the columns; the number of tracks is the number of columns. */
        public Builder columns(Track... columns) {
            if (columns.length == 0) {
                throw new IllegalArgumentException("a grid needs at least one column");
            }
            this.columns = columns.clone();
            return this;
        }

        /** {@code count} auto-sized columns. */
        public Builder columns(int count) {
            if (count < 1) {
                throw new IllegalArgumentException("a grid needs at least one column");
            }
            Track[] tracks = new Track[count];
            Arrays.fill(tracks, Track.auto());
            this.columns = tracks;
            return this;
        }

        /** Define the first rows; any further rows are auto-sized. */
        public Builder rows(Track... rows) {
            this.rows = rows.clone();
            return this;
        }

        public Builder spacing(int horizontal, int vertical) {
            this.hGap = horizontal;
            this.vGap = vertical;
            return this;
        }

        public Builder spacing(int both) {
            return spacing(both, both);
        }

        public Builder padding(Insets insets) {
            this.insets = Objects.requireNonNull(insets);
            return this;
        }

        public Builder padding(int all) {
            return padding(Insets.of(all));
        }

        /** Where widgets without their own alignment hint sit within their cell. */
        public Builder cellAlign(Align horizontal, Align vertical) {
            this.cellH = Objects.requireNonNull(horizontal);
            this.cellV = Objects.requireNonNull(vertical);
            return this;
        }

        public GridLayout build() {
            return new GridLayout(columns, rows, hGap, vGap, insets, cellH, cellV);
        }
    }
}
