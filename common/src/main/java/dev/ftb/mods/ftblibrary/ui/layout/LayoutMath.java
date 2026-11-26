package dev.ftb.mods.ftblibrary.ui.layout;

/**
 * Some integer-arithmetic helpers for layouts.
 */
final class LayoutMath {
    private LayoutMath() {
    }

    /**
     * Offset of an item of the given size within {@code available} pixels. If the item is larger than the available
     * space, it is pinned to the start rather than given a negative offset.
     */
    static int alignOffset(Align align, int available, int size) {
        return switch (align) {
            case START -> 0;
            case CENTER -> Math.max(0, (available - size) / 2);
            case END -> Math.max(0, available - size);
        };
    }

    /**
     * Offsets of items laid out one after another with fixed spacing between them.
     *
     * @param sizes   size of each item along the layout axis
     * @param start   offset of the first item
     * @param spacing gap between consecutive items
     */
    static int[] stackOffsets(int[] sizes, int start, int spacing) {
        int[] offsets = new int[sizes.length];
        int at = start;
        for (int i = 0; i < sizes.length; i++) {
            offsets[i] = at;
            at += sizes[i] + spacing;
        }
        return offsets;
    }

    /**
     * Position just past the last item of a stack, without any trailing spacing; equal to {@code start} when there
     * are no items.
     */
    static int stackEnd(int[] sizes, int start, int spacing) {
        if (sizes.length == 0) {
            return start;
        }
        int end = start;
        for (int size : sizes) {
            end += size;
        }
        return end + spacing * (sizes.length - 1);
    }

    /** Largest value in the array, or 0 if it is empty. */
    static int max(int[] sizes) {
        int max = 0;
        for (int size : sizes) {
            max = Math.max(max, size);
        }
        return max;
    }

    /**
     * Split {@code total} pixels among items in proportion to their weights, honouring per-item bounds. This is the
     * usual "flex" resolution: items whose share would fall outside their bounds are pinned to the bound, and the
     * space they gave up or took is redistributed among the rest.
     * <p>
     * The result is exact: unless every item ends up pinned, the sizes add up to {@code total}. Pixels that do not
     * divide evenly go to the items with the largest fractional share (earlier items win ties). If the minimums
     * alone exceed {@code total}, every item gets its minimum.
     *
     * @param total   pixels to hand out; negative values are treated as zero
     * @param weights relative weights, each at least 1
     * @param mins    lower bound per item
     * @param maxs    upper bound per item
     */
    static int[] distribute(int total, int[] weights, int[] mins, int[] maxs) {
        int n = weights.length;
        int[] result = new int[n];
        boolean[] frozen = new boolean[n];
        long remaining = Math.max(0, total);
        int open = n;

        while (open > 0) {
            long weightSum = 0;
            for (int i = 0; i < n; i++) {
                if (!frozen[i]) {
                    weightSum += weights[i];
                }
            }

            // How far the unconstrained shares fall outside the bounds, in total
            double violation = 0;
            boolean anyViolation = false;
            boolean[] tooSmall = new boolean[n];
            boolean[] tooBig = new boolean[n];
            for (int i = 0; i < n; i++) {
                if (frozen[i]) {
                    continue;
                }
                double ideal = (double) remaining * weights[i] / weightSum;
                if (ideal < mins[i]) {
                    violation += mins[i] - ideal;
                    tooSmall[i] = true;
                    anyViolation = true;
                } else if (ideal > maxs[i]) {
                    violation += maxs[i] - ideal;
                    tooBig[i] = true;
                    anyViolation = true;
                }
            }

            if (!anyViolation) {
                break;
            }

            // Pin the items that broke the bounds in the dominant direction, then try again without them
            boolean pinned = false;
            for (int i = 0; i < n; i++) {
                if (frozen[i]) {
                    continue;
                }
                if (violation >= 0 && tooSmall[i]) {
                    result[i] = mins[i];
                } else if (violation < 0 && tooBig[i]) {
                    result[i] = maxs[i];
                } else {
                    continue;
                }
                frozen[i] = true;
                remaining -= result[i];
                open--;
                pinned = true;
            }
            remaining = Math.max(0, remaining);
            if (!pinned) {
                break;
            }
        }

        if (open == 0) {
            return result;
        }

        // Whole pixels by weight, then hand the leftovers to the largest fractional parts
        long weightSum = 0;
        for (int i = 0; i < n; i++) {
            if (!frozen[i]) {
                weightSum += weights[i];
            }
        }
        long[] fraction = new long[n];
        long given = 0;
        for (int i = 0; i < n; i++) {
            if (!frozen[i]) {
                long exact = remaining * weights[i];
                result[i] = (int) (exact / weightSum);
                fraction[i] = exact % weightSum;
                given += result[i];
            }
        }
        long leftover = remaining - given;
        while (leftover > 0) {
            int best = -1;
            for (int i = 0; i < n; i++) {
                if (!frozen[i] && result[i] < maxs[i] && (best == -1 || fraction[i] > fraction[best])) {
                    best = i;
                }
            }
            if (best == -1) {
                break;
            }
            result[best]++;
            fraction[best] = -1;
            leftover--;
            // everyone has had a turn: start another round
            boolean anyLeft = false;
            for (int i = 0; i < n; i++) {
                if (!frozen[i] && fraction[i] >= 0) {
                    anyLeft = true;
                    break;
                }
            }
            if (!anyLeft) {
                for (int i = 0; i < n; i++) {
                    if (!frozen[i]) {
                        fraction[i] = 0;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Break a sequence of items into lines. An item starts a new line when it does not fit after the previous ones on
     * the current line; the first item of a line is always placed, even if it is wider than the line.
     *
     * @param widths    width of each item
     * @param available room on a line; zero or less means unlimited
     * @param spacing   gap between items on a line
     * @return the line number (from 0) of each item
     */
    static int[] flowLines(int[] widths, int available, int spacing) {
        int[] lines = new int[widths.length];
        int line = 0;
        long used = 0;
        boolean first = true;
        for (int i = 0; i < widths.length; i++) {
            long needed = first ? widths[i] : used + spacing + widths[i];
            if (!first && available > 0 && needed > available) {
                line++;
                used = widths[i];
            } else {
                used = needed;
            }
            first = false;
            lines[i] = line;
        }
        return lines;
    }

    /**
     * The room a layout may use, given a panel's size: a size of zero or less means the panel has no size yet and is
     * treated as {@link Size#UNBOUNDED}.
     */
    static int available(int panelSize) {
        return panelSize > 0 ? panelSize : Size.UNBOUNDED;
    }
}
