package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import dev.ftb.mods.ftblibrary.ui.layout.GridLayout.Track;
import dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.TestPanel;
import org.junit.jupiter.api.Test;

import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.child;
import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.hinted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GridLayoutTest {
    private static TestPanel labelValuePanel(int panelWidth) {
        TestPanel panel = new TestPanel(panelWidth, 0);
        child(panel, 20, 10);
        child(panel, 5, 10);
        child(panel, 30, 12);
        child(panel, 5, 8);
        return panel;
    }

    private static GridLayout labelValueGrid() {
        return WidgetLayout.grid().columns(Track.auto(), Track.weight(1)).spacing(4, 2).build();
    }

    @Test
    void autoAndWeightedColumns() {
        TestPanel panel = labelValuePanel(100);

        int height = panel.align(labelValueGrid());

        var w = panel.getWidgets();
        assertEquals(0, w.get(0).posX);
        assertEquals(0, w.get(0).posY);
        assertEquals(34, w.get(1).posX); // 30 wide first column + 4 gap
        assertEquals(0, w.get(1).posY);
        assertEquals(0, w.get(2).posX);
        assertEquals(12, w.get(2).posY); // 10 high first row + 2 gap
        assertEquals(34, w.get(3).posX);
        assertEquals(10 + 2 + 12, height);
    }

    @Test
    void measureWidthStretchesWithPanelOrFitsContent() {
        assertEquals(100, labelValueGrid().measureWidth(labelValuePanel(100)));
        assertEquals(30 + 4 + 5, labelValueGrid().measureWidth(labelValuePanel(0)));
    }

    @Test
    void paddingOffsetsCellsAndAddsToHeight() {
        TestPanel panel = new TestPanel(0, 0);
        Widget a = child(panel, 10, 10);

        int height = panel.align(WidgetLayout.grid().padding(3).build());

        assertEquals(3, a.posX);
        assertEquals(3, a.posY);
        assertEquals(16, height);
        assertEquals(16, panel.getContentHeight());
    }

    @Test
    void cellAlignmentAndHintOverride() {
        TestPanel panel = new TestPanel(0, 0);
        Widget centred = child(panel, 10, 10);
        Widget end = hinted(panel, 10, 10, LayoutHints.DEFAULT.withHAlign(Align.END));

        panel.align(WidgetLayout.grid()
                .columns(Track.fixed(50), Track.fixed(50))
                .rows(Track.fixed(30))
                .cellAlign(Align.CENTER, Align.CENTER).build());

        assertEquals(20, centred.posX);
        assertEquals(10, centred.posY);
        assertEquals(50 + 40, end.posX);
        assertEquals(10, end.posY);
    }

    @Test
    void resizeFillsCellLessMargins() {
        TestPanel panel = new TestPanel(0, 0);
        Widget w = hinted(panel, 5, 5, LayoutHints.DEFAULT.resize().withMargin(Insets.of(2)));

        panel.align(WidgetLayout.grid().columns(Track.fixed(50)).rows(Track.fixed(30)).build());

        assertEquals(46, w.width);
        assertEquals(26, w.height);
        assertEquals(2, w.posX);
        assertEquals(2, w.posY);
    }

    @Test
    void marginsCountTowardsAutoTracks() {
        TestPanel panel = new TestPanel(0, 0);
        child(panel, 10, 10);
        hinted(panel, 10, 10, LayoutHints.DEFAULT.withMargin(Insets.of(0, 5)));
        Widget next = child(panel, 10, 10);

        panel.align(WidgetLayout.grid().columns(2).build());

        // first row is 10 high; the margin widget is 20 wide, so column 0 is still 10 wide, column 1 is 20
        assertEquals(0, next.posX);
        assertEquals(10, next.posY);
    }

    @Test
    void columnSpanWidensAutoColumnsAndFillsTheSpan() {
        TestPanel panel = new TestPanel(0, 0);
        child(panel, 10, 10);
        Widget b = child(panel, 10, 10);
        Widget wide = hinted(panel, 30, 10, LayoutHints.DEFAULT.withSpan(2, 1).withHFlex(Flex.RESIZE));

        int height = panel.align(WidgetLayout.grid().columns(2).build());

        assertEquals(15, b.posX); // both columns grew from 10 to 15
        assertEquals(30, wide.width);
        assertEquals(10, wide.posY);
        assertEquals(20, height);
    }

    @Test
    void rowSpanWidensAutoRows() {
        TestPanel panel = new TestPanel(0, 0);
        child(panel, 10, 30).setLayoutHints(LayoutHints.DEFAULT.withSpan(1, 2));
        child(panel, 10, 10);
        Widget c = child(panel, 10, 10);

        int height = panel.align(WidgetLayout.grid().columns(2).build());

        assertEquals(10, c.posX);
        assertEquals(15, c.posY);
        assertEquals(30, height);
    }

    @Test
    void weightedRowsShareTheHeight() {
        TestPanel panel = new TestPanel(0, 100);
        child(panel, 10, 10);
        child(panel, 10, 10);
        Widget bottom = child(panel, 10, 10);

        int height = panel.align(WidgetLayout.grid().rows(Track.fixed(20), Track.weight(1), Track.weight(1)).build());

        assertEquals(20 + 40, bottom.posY);
        assertEquals(100, height);
    }

    @Test
    void emptyGridHasOnlyInsets() {
        assertEquals(6, new TestPanel(10, 10).align(WidgetLayout.grid().padding(3).build()));
    }

    @Test
    void builderRejectsEmptyColumns() {
        assertThrows(IllegalArgumentException.class, () -> WidgetLayout.grid().columns());
        assertThrows(IllegalArgumentException.class, () -> WidgetLayout.grid().columns(0));
    }

    @Test
    void layoutIsReusable() {
        GridLayout shared = WidgetLayout.grid().columns(2).spacing(1).build();
        TestPanel a = labelValuePanel(0);
        TestPanel b = new TestPanel(0, 0);
        child(b, 7, 7);

        assertEquals(10 + 1 + 12, a.align(shared));
        assertEquals(7, b.align(shared));
    }
}
