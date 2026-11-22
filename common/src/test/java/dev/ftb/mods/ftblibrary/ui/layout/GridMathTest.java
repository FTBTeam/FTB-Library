package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.layout.GridLayout.Track;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GridMathTest {
    private static int[] ones(int n) {
        int[] a = new int[n];
        Arrays.fill(a, 1);
        return a;
    }

    @Test
    void placeFillsRowsLeftToRight() {
        int[][] cells = GridMath.place(3, ones(5), ones(5));
        assertArrayEquals(new int[]{0, 0}, cells[0]);
        assertArrayEquals(new int[]{1, 0}, cells[1]);
        assertArrayEquals(new int[]{2, 0}, cells[2]);
        assertArrayEquals(new int[]{0, 1}, cells[3]);
        assertArrayEquals(new int[]{1, 1}, cells[4]);
        assertEquals(2, GridMath.rowCount(cells, ones(5)));
    }

    @Test
    void placeWrapsWhenSpanDoesNotFit() {
        int[][] cells = GridMath.place(3, new int[]{1, 2, 1}, ones(3));
        assertArrayEquals(new int[]{0, 0}, cells[0]);
        assertArrayEquals(new int[]{1, 0}, cells[1]);
        assertArrayEquals(new int[]{0, 1}, cells[2]);
    }

    @Test
    void placeFlowsAroundRowSpans() {
        int[][] cells = GridMath.place(2, ones(3), new int[]{2, 1, 1});
        assertArrayEquals(new int[]{0, 0}, cells[0]);
        assertArrayEquals(new int[]{1, 0}, cells[1]);
        assertArrayEquals(new int[]{1, 1}, cells[2]);
        assertEquals(2, GridMath.rowCount(cells, new int[]{2, 1, 1}));
    }

    @Test
    void placeClampsColumnSpanToGridWidth() {
        int[][] cells = GridMath.place(2, new int[]{5, 1}, ones(2));
        assertArrayEquals(new int[]{0, 0}, cells[0]);
        assertArrayEquals(new int[]{0, 1}, cells[1]);
    }

    @Test
    void placeOfNothingIsEmpty() {
        assertEquals(0, GridMath.place(3, new int[0], new int[0]).length);
        assertEquals(0, GridMath.rowCount(new int[0][], new int[0]));
    }

    @Test
    void trackContentTakesLargestSingleSpanItem() {
        int[] content = GridMath.trackContent(2, new int[]{0, 1, 0}, ones(3), new int[]{10, 20, 15}, new boolean[]{true, true}, 4);
        assertArrayEquals(new int[]{15, 20}, content);
    }

    @Test
    void trackContentWidensTracksForSpanningItems() {
        boolean[] both = {true, true};
        assertArrayEquals(new int[]{18, 28},
                GridMath.trackContent(2, new int[]{0, 1, 0}, new int[]{1, 1, 2}, new int[]{10, 20, 50}, both, 4));
        assertArrayEquals(new int[]{19, 28},
                GridMath.trackContent(2, new int[]{0, 1, 0}, new int[]{1, 1, 2}, new int[]{10, 20, 51}, both, 4));
    }

    @Test
    void trackContentOnlyGrowsGrowableTracks() {
        assertArrayEquals(new int[]{26, 20},
                GridMath.trackContent(2, new int[]{0, 1, 0}, new int[]{1, 1, 2}, new int[]{10, 20, 50}, new boolean[]{true, false}, 4));
    }

    @Test
    void trackContentIgnoresSpansWhenSpanningItemFits() {
        assertArrayEquals(new int[]{10, 20},
                GridMath.trackContent(2, new int[]{0, 1, 0}, new int[]{1, 1, 2}, new int[]{10, 20, 30}, new boolean[]{true, true}, 4));
    }

    @Test
    void sizeTracksWeightedTrackTakesLeftover() {
        int[] sizes = GridMath.sizeTracks(new Track[]{Track.auto(), Track.weight(1)}, new int[]{10, 5}, 100, 4);
        assertArrayEquals(new int[]{10, 86}, sizes);
    }

    @Test
    void sizeTracksSplitsByWeight() {
        int[] sizes = GridMath.sizeTracks(new Track[]{Track.weight(1), Track.weight(3)}, new int[]{0, 0}, 100, 0);
        assertArrayEquals(new int[]{25, 75}, sizes);
    }

    @Test
    void sizeTracksNeverShrinkBelowContent() {
        int[] sizes = GridMath.sizeTracks(new Track[]{Track.weight(1), Track.weight(1)}, new int[]{70, 0}, 100, 0);
        assertArrayEquals(new int[]{70, 30}, sizes);
    }

    @Test
    void sizeTracksFixedIgnoresContent() {
        int[] sizes = GridMath.sizeTracks(new Track[]{Track.fixed(30), Track.weight(1)}, new int[]{99, 0}, 100, 10);
        assertArrayEquals(new int[]{30, 60}, sizes);
    }

    @Test
    void sizeTracksUseContentWhenRoomIsUnknown() {
        int[] sizes = GridMath.sizeTracks(new Track[]{Track.auto(), Track.weight(1)}, new int[]{10, 5}, -1, 4);
        assertArrayEquals(new int[]{10, 5}, sizes);
    }

    @Test
    void spanSizeIncludesGaps() {
        assertEquals(10 + 4 + 20, GridMath.spanSize(new int[]{10, 20, 30}, 0, 2, 4));
        assertEquals(30, GridMath.spanSize(new int[]{10, 20, 30}, 2, 3, 4));
    }

    @Test
    void trackNormalisesValues() {
        assertEquals(1, Track.weight(0).value());
        assertEquals(0, Track.fixed(-5).value());
    }
}
