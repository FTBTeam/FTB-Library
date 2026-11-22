package dev.ftb.mods.ftblibrary.ui.layout;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LayoutMathTest {
    @Test
    void alignOffsets() {
        assertEquals(0, LayoutMath.alignOffset(Align.START, 100, 30));
        assertEquals(35, LayoutMath.alignOffset(Align.CENTER, 100, 30));
        assertEquals(70, LayoutMath.alignOffset(Align.END, 100, 30));
    }

    @Test
    void centerRoundsTowardStart() {
        assertEquals(1, LayoutMath.alignOffset(Align.CENTER, 10, 7));
    }

    @Test
    void oversizedItemsArePinnedToStart() {
        assertEquals(0, LayoutMath.alignOffset(Align.CENTER, 10, 30));
        assertEquals(0, LayoutMath.alignOffset(Align.END, 10, 30));
    }

    @Test
    void stackOffsets() {
        assertArrayEquals(new int[]{2, 14, 28}, LayoutMath.stackOffsets(new int[]{10, 12, 5}, 2, 2));
        assertArrayEquals(new int[0], LayoutMath.stackOffsets(new int[0], 2, 2));
    }

    @Test
    void stackEnd() {
        assertEquals(2 + 27 + 4, LayoutMath.stackEnd(new int[]{10, 12, 5}, 2, 2));
        assertEquals(2 + 10, LayoutMath.stackEnd(new int[]{10}, 2, 5));
        assertEquals(2, LayoutMath.stackEnd(new int[0], 2, 5));
    }

    @Test
    void max() {
        assertEquals(12, LayoutMath.max(new int[]{10, 12, 5}));
        assertEquals(0, LayoutMath.max(new int[0]));
    }

    private static int[] ones(int n) {
        int[] a = new int[n];
        Arrays.fill(a, 1);
        return a;
    }

    private static int[] filled(int n, int v) {
        int[] a = new int[n];
        Arrays.fill(a, v);
        return a;
    }

    private static int sum(int[] a) {
        return Arrays.stream(a).sum();
    }

    @Test
    void distributeByWeight() {
        assertArrayEquals(new int[]{30, 60}, LayoutMath.distribute(90, new int[]{1, 2}, filled(2, 0), filled(2, Integer.MAX_VALUE)));
    }

    @Test
    void distributeHandsOutRemainderPixelsToEarlierItems() {
        int[] none = filled(3, Integer.MAX_VALUE);
        assertArrayEquals(new int[]{34, 33, 33}, LayoutMath.distribute(100, ones(3), filled(3, 0), none));
        assertArrayEquals(new int[]{4, 4, 3}, LayoutMath.distribute(11, ones(3), filled(3, 0), none));
        assertArrayEquals(new int[]{1, 1, 0}, LayoutMath.distribute(2, ones(3), filled(3, 0), none));
    }

    @Test
    void distributeAlwaysAddsUpWhenUnbounded() {
        for (int total = 0; total < 60; total++) {
            for (int n = 1; n <= 6; n++) {
                int[] weights = new int[n];
                for (int i = 0; i < n; i++) {
                    weights[i] = 1 + (i * 7 + total) % 5;
                }
                int[] result = LayoutMath.distribute(total, weights, filled(n, 0), filled(n, Integer.MAX_VALUE));
                assertEquals(total, sum(result), "total=" + total + " n=" + n);
            }
        }
    }

    @Test
    void distributeHonoursMinimumAndRedistributes() {
        assertArrayEquals(new int[]{70, 30}, LayoutMath.distribute(100, ones(2), new int[]{70, 0}, filled(2, Integer.MAX_VALUE)));
    }

    @Test
    void distributeHonoursMaximumAndRedistributes() {
        assertArrayEquals(new int[]{20, 80}, LayoutMath.distribute(100, ones(2), filled(2, 0), new int[]{20, Integer.MAX_VALUE}));
        assertArrayEquals(new int[]{10, 10}, LayoutMath.distribute(100, ones(2), filled(2, 0), filled(2, 10)));
    }

    @Test
    void distributeGivesMinimumsWhenSpaceRunsOut() {
        assertArrayEquals(new int[]{8, 8}, LayoutMath.distribute(10, ones(2), filled(2, 8), filled(2, Integer.MAX_VALUE)));
        assertArrayEquals(new int[]{0, 0}, LayoutMath.distribute(-5, ones(2), filled(2, 0), filled(2, Integer.MAX_VALUE)));
        assertArrayEquals(new int[]{8, 8}, LayoutMath.distribute(0, ones(2), filled(2, 8), filled(2, Integer.MAX_VALUE)));
    }

    @Test
    void distributeOfNothingIsEmpty() {
        assertArrayEquals(new int[0], LayoutMath.distribute(50, new int[0], new int[0], new int[0]));
    }

    @Test
    void flowLinesWrapWhenFull() {
        assertArrayEquals(new int[]{0, 0, 1}, LayoutMath.flowLines(new int[]{30, 30, 30}, 70, 5));
        assertArrayEquals(new int[]{0, 0, 1, 1}, LayoutMath.flowLines(new int[]{30, 30, 30, 30}, 70, 5));
    }

    @Test
    void flowLinesNeverWrapWithoutLimit() {
        assertArrayEquals(new int[]{0, 0, 0}, LayoutMath.flowLines(new int[]{30, 30, 30}, 0, 5));
    }

    @Test
    void flowLinesGiveOversizedItemsALineOfTheirOwn() {
        assertArrayEquals(new int[]{0, 1}, LayoutMath.flowLines(new int[]{100, 10}, 50, 0));
        assertArrayEquals(new int[]{0, 1, 2}, LayoutMath.flowLines(new int[]{10, 100, 10}, 50, 0));
        assertArrayEquals(new int[0], LayoutMath.flowLines(new int[0], 50, 0));
    }
}
