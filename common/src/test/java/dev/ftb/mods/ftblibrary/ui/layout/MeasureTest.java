package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.layout.GridLayout.Track;
import dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.TestPanel;
import dev.ftb.mods.ftblibrary.ui.misc.CompactGridLayout;
import org.junit.jupiter.api.Test;

import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.child;
import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.hinted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MeasureTest {
    private static TestPanel twoChildren(int panelW, int panelH) {
        TestPanel panel = new TestPanel(panelW, panelH);
        child(panel, 10, 10);
        child(panel, 30, 20);
        return panel;
    }

    // ---- linear

    @Test
    void verticalIsStackedHeightByWidest() {
        TestPanel panel = twoChildren(0, 0);
        Size size = WidgetLayout.vertical().padding(new Insets(2, 3, 4, 5)).spacing(1).build().measure(panel);
        assertEquals(new Size(30 + 8, 2 + 10 + 1 + 20 + 4), size);
    }

    @Test
    void horizontalIsStackedWidthByTallest() {
        TestPanel panel = twoChildren(0, 0);
        Size size = WidgetLayout.horizontal().padding(new Insets(2, 3, 4, 5)).spacing(1).build().measure(panel);
        assertEquals(new Size(5 + 10 + 1 + 30 + 3, 20 + 6), size);
    }

    @Test
    void measureIgnoresThePanelsOwnSize() {
        LinearLayout layout = WidgetLayout.vertical().spacing(2).build();
        assertEquals(layout.measure(twoChildren(0, 0)), layout.measure(twoChildren(300, 200)));
    }

    @Test
    void resizingWidgetsCountAtTheirCurrentClampedSize() {
        TestPanel panel = new TestPanel(100, 100);
        child(panel, 10, 10);
        hinted(panel, 10, 5, LayoutHints.DEFAULT.withVFlex(Flex.RESIZE).withMinSize(0, 8));

        Size size = WidgetLayout.vertical().build().measure(panel);

        assertEquals(new Size(10, 18), size);
    }

    @Test
    void marginsCountOnBothAxes() {
        TestPanel panel = new TestPanel(0, 0);
        hinted(panel, 10, 10, LayoutHints.DEFAULT.withMargin(new Insets(1, 2, 3, 4)));

        assertEquals(new Size(10 + 2 + 4, 1 + 10 + 3), WidgetLayout.vertical().build().measure(panel));
    }

    @Test
    void explicitCrossSizeIsWhatIsWanted() {
        TestPanel panel = twoChildren(0, 0);
        Size size = WidgetLayout.vertical().padding(Insets.of(0, 3)).crossSize(50).build().measure(panel);
        assertEquals(56, size.width());
    }

    @Test
    void emptyLinearWantsOnlyItsInsets() {
        Size size = WidgetLayout.vertical().padding(new Insets(2, 3, 4, 5)).build().measure(new TestPanel(0, 0));
        assertEquals(new Size(8, 6), size);
    }

    @Test
    void measureDoesNotMoveOrResizeAnything() {
        TestPanel panel = new TestPanel(100, 100);
        Widget a = hinted(panel, 10, 10, LayoutHints.DEFAULT.resize());
        Widget b = child(panel, 20, 20);
        a.setPos(7, 8);
        b.setPos(9, 11);

        for (WidgetLayout layout : new WidgetLayout[]{
                WidgetLayout.vertical().fillCross().build(),
                WidgetLayout.horizontal().build(),
                WidgetLayout.stack().build(),
                WidgetLayout.flow().build(),
                WidgetLayout.grid().columns(2).build(),
                new WidgetLayout.Vertical(1, 1, 1)}) {
            layout.measure(panel);

            assertEquals(7, a.posX);
            assertEquals(8, a.posY);
            assertEquals(10, a.width);
            assertEquals(10, a.height);
            assertEquals(9, b.posX);
            assertEquals(11, b.posY);
            assertEquals(20, b.width);
            assertEquals(20, b.height);
        }
    }

    @Test
    void linearMeasureMatchesAlignWhenNothingResizes() {
        LinearLayout v = WidgetLayout.vertical().padding(new Insets(2, 3, 4, 5)).spacing(1).build();
        TestPanel a = twoChildren(0, 0);
        assertEquals(v.measure(a).height(), a.align(v));

        LinearLayout h = WidgetLayout.horizontal().padding(new Insets(2, 3, 4, 5)).spacing(1).build();
        TestPanel b = twoChildren(0, 0);
        assertEquals(h.measure(b).width(), b.align(h));
    }

    // ---- stack

    @Test
    void stackWantsTheLargestWidgetPlusInsets() {
        TestPanel panel = new TestPanel(0, 0);
        child(panel, 20, 10);
        child(panel, 30, 40);

        Size size = WidgetLayout.stack().padding(Insets.of(5, 3)).build().measure(panel);

        assertEquals(new Size(36, 50), size);
    }

    @Test
    void stackCountsMarginsAndMatchesAlign() {
        TestPanel panel = new TestPanel(0, 0);
        hinted(panel, 20, 10, LayoutHints.DEFAULT.withMargin(new Insets(1, 2, 3, 4)));
        StackLayout layout = WidgetLayout.stack().padding(2).build();

        Size size = layout.measure(panel);

        assertEquals(new Size(20 + 6 + 4, 10 + 4 + 4), size);
        assertEquals(size.height(), panel.align(layout));
    }

    // ---- flow

    private static TestPanel threeWide(int panelWidth) {
        TestPanel panel = new TestPanel(panelWidth, 0);
        child(panel, 30, 10);
        child(panel, 30, 10);
        child(panel, 30, 10);
        return panel;
    }

    @Test
    void flowWithoutALimitIsOneLine() {
        FlowLayout layout = WidgetLayout.flow().spacing(5, 2).build();
        assertEquals(new Size(100, 10), layout.measure(threeWide(0)));
        assertEquals(new Size(100, 10), layout.measure(threeWide(70))); // unbounded ignores the panel's own width
    }

    @Test
    void flowWrapsToTheAvailableWidth() {
        FlowLayout layout = WidgetLayout.flow().spacing(5, 2).build();
        assertEquals(new Size(65, 22), layout.measure(threeWide(0), 70, Size.UNBOUNDED));
    }

    @Test
    void flowInsetsShrinkTheLineAndGrowTheResult() {
        FlowLayout layout = WidgetLayout.flow().padding(5).spacing(5, 2).build();
        assertEquals(new Size(65 + 10, 22 + 10), layout.measure(threeWide(0), 80, Size.UNBOUNDED));
    }

    @Test
    void flowWithNoRoomPutsEachWidgetOnItsOwnLine() {
        FlowLayout layout = WidgetLayout.flow().spacing(5, 2).build();
        assertEquals(new Size(30, 34), layout.measure(threeWide(0), 0, Size.UNBOUNDED));
    }

    @Test
    void flowMeasureMatchesAlignForTheSameWidth() {
        FlowLayout layout = WidgetLayout.flow().padding(3).spacing(4, 2).build();
        TestPanel panel = threeWide(60);

        int measured = layout.measure(panel, 60, Size.UNBOUNDED).height();

        assertEquals(measured, panel.align(layout));
    }

    @Test
    void flowLinesAreComputedFromFootprints() {
        FlowLayout.Lines lines = FlowLayout.Lines.compute(new int[]{30, 30, 30}, new int[]{10, 12, 8}, 70, 5);

        org.junit.jupiter.api.Assertions.assertArrayEquals(new int[]{0, 0, 1}, lines.lineOf());
        org.junit.jupiter.api.Assertions.assertArrayEquals(new int[]{65, 30}, lines.lineWidth());
        org.junit.jupiter.api.Assertions.assertArrayEquals(new int[]{12, 8}, lines.lineHeight());
    }

    @Test
    void flowLinesOfNothingAreEmpty() {
        FlowLayout.Lines lines = FlowLayout.Lines.compute(new int[0], new int[0], 70, 5);
        assertEquals(0, lines.lineWidth().length);
        assertEquals(0, lines.lineHeight().length);
    }

    // ---- grid

    private static TestPanel labelValuePanel(int panelWidth, int panelHeight) {
        TestPanel panel = new TestPanel(panelWidth, panelHeight);
        child(panel, 20, 10);
        child(panel, 5, 10);
        child(panel, 30, 12);
        child(panel, 5, 8);
        return panel;
    }

    @Test
    void gridWantsContentSizedTracks() {
        GridLayout layout = WidgetLayout.grid().columns(Track.auto(), Track.weight(1)).spacing(4, 2).build();

        // weighted columns count at content size, so the panel's own width makes no difference
        assertEquals(new Size(30 + 4 + 5, 10 + 2 + 12), layout.measure(labelValuePanel(0, 0)));
        assertEquals(new Size(30 + 4 + 5, 10 + 2 + 12), layout.measure(labelValuePanel(100, 100)));
    }

    @Test
    void gridKeepsFixedTracksAndAddsInsets() {
        GridLayout layout = WidgetLayout.grid().columns(Track.fixed(50)).padding(3).build();
        TestPanel panel = new TestPanel(0, 0);
        child(panel, 10, 10);

        assertEquals(new Size(56, 16), layout.measure(panel));
    }

    @Test
    void gridMeasureMatchesAlignAndMeasureWidth() {
        GridLayout layout = WidgetLayout.grid().columns(Track.auto(), Track.weight(1)).spacing(4, 2).build();
        TestPanel panel = labelValuePanel(0, 0);

        Size size = layout.measure(panel);

        assertEquals(layout.measureWidth(panel), size.width());
        assertEquals(size.height(), panel.align(layout));
    }

    // ---- legacy and default

    @Test
    void legacyVerticalAndHorizontalMeasureMatchAlign() {
        WidgetLayout.Vertical v = new WidgetLayout.Vertical(2, 1, 3);
        TestPanel a = twoChildren(0, 0);
        assertEquals(new Size(30, 2 + 10 + 1 + 20 + 3), v.measure(a));
        assertEquals(v.measure(a).height(), a.align(v));

        WidgetLayout.Horizontal h = new WidgetLayout.Horizontal(2, 1, 3);
        TestPanel b = twoChildren(0, 0);
        assertEquals(new Size(2 + 10 + 1 + 30 + 3, 20), h.measure(b));
        assertEquals(h.measure(b).width(), b.align(h));
    }

    @Test
    void emptyLegacyLayoutWantsOnlyItsPadding() {
        assertEquals(new Size(0, 5), new WidgetLayout.Vertical(2, 1, 3).measure(new TestPanel(0, 0)));
        assertEquals(new Size(5, 0), new WidgetLayout.Horizontal(2, 1, 3).measure(new TestPanel(0, 0)));
    }

    @Test
    void layoutsThatCannotMeasureSayNothing() {
        TestPanel panel = twoChildren(0, 0);
        assertNull(WidgetLayout.NONE.measure(panel));
        assertNull(new CompactGridLayout(10).measure(panel));
    }

    @Test
    void sizeConstants() {
        assertEquals(-1, Size.UNBOUNDED);
        assertEquals(new Size(0, 0), Size.ZERO);
    }
}
