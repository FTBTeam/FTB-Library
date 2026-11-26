package dev.ftb.mods.ftblibrary.ui.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * Placement and track-sizing helpers for {@link GridLayout}.
 */
final class GridMath {
    private GridMath() {
    }

    /**
     * Put items into a grid, left to right and then top to bottom, skipping cells that earlier (spanning) items
     * already cover. Placement is "sparse": it never goes back to fill gaps left behind.
     *
     * @param columns  number of columns
     * @param colSpans columns each item covers; values are clamped to {@code 1..columns}
     * @param rowSpans rows each item covers; values below 1 count as 1
     * @return {column, row} for each item
     */
    static int[][] place(int columns, int[] colSpans, int[] rowSpans) {
        int n = colSpans.length;
        int[][] result = new int[n][2];
        List<boolean[]> taken = new ArrayList<>();
        int col = 0;
        int row = 0;

        for (int i = 0; i < n; i++) {
            int cs = Math.clamp(colSpans[i], 1, columns);
            int rs = Math.max(1, rowSpans[i]);

            while (true) {
                if (col + cs > columns) {
                    col = 0;
                    row++;
                }
                if (fits(taken, col, row, cs, rs)) {
                    break;
                }
                col++;
            }

            for (int r = row; r < row + rs; r++) {
                while (taken.size() <= r) {
                    taken.add(new boolean[columns]);
                }
                for (int c = col; c < col + cs; c++) {
                    taken.get(r)[c] = true;
                }
            }
            result[i][0] = col;
            result[i][1] = row;
            col += cs;
        }
        return result;
    }

    private static boolean fits(List<boolean[]> taken, int col, int row, int cs, int rs) {
        for (int r = row; r < row + rs && r < taken.size(); r++) {
            for (int c = col; c < col + cs; c++) {
                if (taken.get(r)[c]) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Number of rows needed by items placed with {@link #place}. */
    static int rowCount(int[][] cells, int[] rowSpans) {
        int rows = 0;
        for (int i = 0; i < cells.length; i++) {
            rows = Math.max(rows, cells[i][1] + Math.max(1, rowSpans[i]));
        }
        return rows;
    }

    /**
     * Content size of each track: the largest item that sits in it alone, then widened where a spanning item needs
     * more than the tracks it covers provide. The shortfall is shared evenly among the tracks that may grow.
     *
     * @param tracks    number of tracks
     * @param start     first track of each item
     * @param span      number of tracks each item covers
     * @param sizes     size each item needs
     * @param growable  whether a track may be widened for a spanning item (false for fixed-size tracks)
     * @param gap       gap between tracks
     */
    static int[] trackContent(int tracks, int[] start, int[] span, int[] sizes, boolean[] growable, int gap) {
        int[] content = new int[tracks];
        for (int i = 0; i < start.length; i++) {
            if (span[i] == 1) {
                content[start[i]] = Math.max(content[start[i]], sizes[i]);
            }
        }
        for (int i = 0; i < start.length; i++) {
            int s = Math.min(span[i], tracks - start[i]);
            if (s <= 1) {
                continue;
            }
            int have = gap * (s - 1);
            int grow = 0;
            for (int t = start[i]; t < start[i] + s; t++) {
                have += content[t];
                if (growable[t]) {
                    grow++;
                }
            }
            int deficit = sizes[i] - have;
            if (deficit > 0 && grow > 0) {
                int each = deficit / grow;
                int extra = deficit % grow;
                for (int t = start[i]; t < start[i] + s; t++) {
                    if (growable[t]) {
                        content[t] += each + (extra-- > 0 ? 1 : 0);
                    }
                }
            }
        }
        return content;
    }

    /**
     * Final size of each track. Auto tracks take their content size, fixed tracks their fixed size. Weighted tracks
     * share what is left of {@code available}, but never get less than their content. With no known room
     * ({@code available} negative) weighted tracks just take their content size.
     */
    static int[] sizeTracks(GridLayout.Track[] tracks, int[] content, int available, int gap) {
        int n = tracks.length;
        int[] sizes = new int[n];
        long used = (long) gap * Math.max(0, n - 1);
        int weighted = 0;

        for (int i = 0; i < n; i++) {
            switch (tracks[i].kind()) {
                case FIXED -> sizes[i] = tracks[i].value();
                case AUTO -> sizes[i] = content[i];
                case WEIGHT -> {
                    sizes[i] = content[i];
                    weighted++;
                    continue;
                }
            }
            used += sizes[i];
        }

        if (weighted == 0 || available < 0) {
            return sizes;
        }

        int[] weights = new int[weighted];
        int[] mins = new int[weighted];
        int[] maxs = new int[weighted];
        int k = 0;
        for (int i = 0; i < n; i++) {
            if (tracks[i].kind() == GridLayout.Track.Kind.WEIGHT) {
                weights[k] = tracks[i].value();
                mins[k] = content[i];
                maxs[k] = LayoutHints.UNBOUNDED;
                k++;
            }
        }
        int[] shares = LayoutMath.distribute(Math.clamp(available - used, 0, Integer.MAX_VALUE), weights, mins, maxs);
        k = 0;
        for (int i = 0; i < n; i++) {
            if (tracks[i].kind() == GridLayout.Track.Kind.WEIGHT) {
                sizes[i] = shares[k++];
            }
        }
        return sizes;
    }

    /** Offset of each track's start, given the offset of the first and the gap between tracks. */
    static int[] trackOffsets(int[] sizes, int start, int gap) {
        return LayoutMath.stackOffsets(sizes, start, gap);
    }

    /** Combined size of {@code span} tracks beginning at {@code start}, including the gaps between them. */
    static int spanSize(int[] sizes, int start, int span, int gap) {
        int end = Math.min(sizes.length, start + span);
        int total = 0;
        for (int t = start; t < end; t++) {
            total += sizes[t];
        }
        return total + gap * Math.max(0, end - start - 1);
    }
}
