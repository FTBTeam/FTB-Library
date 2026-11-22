package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.layout.GridLayout.Track;
import dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.TestPanel;
import org.junit.jupiter.api.Test;

import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.child;
import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.hinted;
import static dev.ftb.mods.ftblibrary.ui.layout.Size.UNBOUNDED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstraintsTest {
    private static final LayoutHints RESIZE_V = LayoutHints.DEFAULT.withVFlex(Flex.RESIZE);
    private static final LayoutHints RESIZE_H = LayoutHints.DEFAULT.withHFlex(Flex.RESIZE);

    private static void assertSamePlacement(TestPanel expected, TestPanel actual) {
        assertEquals(expected.getWidgets().size(), actual.getWidgets().size());
        for (int i = 0; i < expected.getWidgets().size(); i++) {
            Widget e = expected.getWidgets().get(i);
            Widget a = actual.getWidgets().get(i);
            assertEquals(e.posX, a.posX, "posX " + i);
            assertEquals(e.posY, a.posY, "posY " + i);
            assertEquals(e.width, a.width, "width " + i);
            assertEquals(e.height, a.height, "height " + i);
        }
    }

    // ---- linear

    @Test
    void unboundedMainStopsResizeFillingTheStalePanelSize() {
        TestPanel panel = new TestPanel(100, 100);
        child(panel, 40, 10);
        Widget fill = hinted(panel, 40, 5, RESIZE_V);
        LinearLayout layout = WidgetLayout.vertical().build();

        int extent = layout.align(panel, UNBOUNDED, UNBOUNDED);

        assertEquals(5, fill.height);
        assertEquals(15, extent);
    }

    @Test
    void plainAlignStillFillsThePanel() {
        TestPanel panel = new TestPanel(100, 100);
        child(panel, 40, 10);
        Widget fill = hinted(panel, 40, 5, RESIZE_V);

        panel.align(WidgetLayout.vertical().build());

        assertEquals(90, fill.height);
    }

    @Test
    void explicitRoomIsUsedOnAPanelWithNoSize() {
        TestPanel panel = new TestPanel(0, 0);
        child(panel, 10, 10);
        Widget fill = hinted(panel, 10, 5, RESIZE_V);

        int extent = WidgetLayout.vertical().padding(Insets.of(0, 4)).fillCross().build().align(panel, 40, 100);

        assertEquals(90, fill.height);
        assertEquals(100, extent);
        assertEquals(32, fill.width); // 40 less the 4 + 4 cross insets
        assertEquals(4, fill.posX);
    }

    @Test
    void explicitRoomOverridesThePanelsOwnSize() {
        TestPanel panel = new TestPanel(200, 200);
        Widget fill = hinted(panel, 10, 5, RESIZE_V);

        WidgetLayout.vertical().build().align(panel, 50, 60);

        assertEquals(60, fill.height);
    }

    @Test
    void zeroRoomIsARealLimitNotUnknown() {
        TestPanel panel = new TestPanel(100, 100);
        child(panel, 10, 10);
        Widget fill = hinted(panel, 10, 20, RESIZE_V.withMinSize(0, 4));

        int extent = WidgetLayout.vertical().build().align(panel, UNBOUNDED, 0);

        assertEquals(4, fill.height);
        assertEquals(14, extent);
    }

    @Test
    void horizontalUsesTheWidthAsItsMainAxis() {
        TestPanel panel = new TestPanel(0, 0);
        child(panel, 10, 10);
        Widget fill = hinted(panel, 5, 10, RESIZE_H);

        WidgetLayout.horizontal().spacing(2).build().align(panel, 100, UNBOUNDED);

        assertEquals(100 - 10 - 2, fill.width);
    }

    @Test
    void unboundedCrossFitsTheWidestWidgetNotThePanel() {
        TestPanel panel = new TestPanel(100, 100);
        Widget narrow = child(panel, 10, 10);
        Widget wide = child(panel, 30, 10);

        WidgetLayout.vertical().fillCross().build().align(panel, UNBOUNDED, UNBOUNDED);

        assertEquals(30, narrow.width);
        assertEquals(30, wide.width);
    }

    @Test
    void explicitCrossSizeStillWinsOverTheRoom() {
        TestPanel panel = new TestPanel(0, 0);
        Widget w = child(panel, 10, 10);

        WidgetLayout.vertical().crossSize(50).fillCross().build().align(panel, 200, 200);

        assertEquals(50, w.width);
    }

    @Test
    void alignOfAPanelIsAlignWithItsOwnSize() {
        for (int[] size : new int[][]{{0, 0}, {80, 0}, {0, 90}, {80, 90}}) {
            TestPanel viaPanel = new TestPanel(size[0], size[1]);
            TestPanel viaRoom = new TestPanel(size[0], size[1]);
            for (TestPanel p : new TestPanel[]{viaPanel, viaRoom}) {
                child(p, 10, 10);
                hinted(p, 10, 5, LayoutHints.DEFAULT.resize());
                child(p, 25, 8);
            }
            LinearLayout layout = WidgetLayout.vertical().padding(2).spacing(1).fillCross().build();

            int a = layout.align(viaPanel);
            int b = layout.align(viaRoom, size[0] > 0 ? size[0] : UNBOUNDED, size[1] > 0 ? size[1] : UNBOUNDED);

            assertEquals(a, b);
            assertSamePlacement(viaPanel, viaRoom);
        }
    }

    // ---- stack

    @Test
    void stackFillsTheRoomItIsGiven() {
        TestPanel panel = new TestPanel(0, 0);
        Widget bg = hinted(panel, 1, 1, LayoutHints.DEFAULT.resize());

        WidgetLayout.stack().build().align(panel, 100, 50);

        assertEquals(100, bg.width);
        assertEquals(50, bg.height);
    }

    @Test
    void stackWithUnboundedRoomFitsTheLargestWidget() {
        TestPanel panel = new TestPanel(100, 50);
        Widget bg = hinted(panel, 1, 1, LayoutHints.DEFAULT.resize());
        child(panel, 30, 20);

        WidgetLayout.stack().build().align(panel, UNBOUNDED, UNBOUNDED);

        assertEquals(30, bg.width);
        assertEquals(20, bg.height);
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
    void flowWrapsToTheRoomItIsGiven() {
        TestPanel panel = threeWide(0);

        int height = WidgetLayout.flow().spacing(5, 2).build().align(panel, 70, UNBOUNDED);

        assertEquals(22, height);
        assertEquals(35, panel.getWidgets().get(1).posX);
        assertEquals(0, panel.getWidgets().get(2).posX);
    }

    @Test
    void flowWithUnboundedRoomIsOneLineWhateverThePanelWidth() {
        TestPanel panel = threeWide(70);

        int height = WidgetLayout.flow().spacing(5, 2).build().align(panel, UNBOUNDED, UNBOUNDED);

        assertEquals(10, height);
        assertEquals(70, panel.getWidgets().get(2).posX);
    }

    @Test
    void flowWithZeroRoomGivesEveryWidgetItsOwnLineAndAgreesWithMeasure() {
        FlowLayout layout = WidgetLayout.flow().spacing(5, 2).build();
        TestPanel panel = threeWide(0);

        int height = layout.align(panel, 0, UNBOUNDED);

        assertEquals(34, height);
        assertEquals(height, layout.measure(panel, 0, UNBOUNDED).height());
    }

    // ---- grid

    private static TestPanel labelAndValue(int panelWidth) {
        TestPanel panel = new TestPanel(panelWidth, 0);
        child(panel, 20, 10);
        hinted(panel, 5, 10, RESIZE_H);
        return panel;
    }

    @Test
    void gridStretchesWeightedColumnsToTheRoomItIsGiven() {
        GridLayout layout = WidgetLayout.grid().columns(Track.auto(), Track.weight(1)).spacing(4, 2).build();
        TestPanel panel = labelAndValue(0);

        layout.align(panel, 100, UNBOUNDED);

        assertEquals(24, panel.getWidgets().get(1).posX);
        assertEquals(100 - 20 - 4, panel.getWidgets().get(1).width);
    }

    @Test
    void gridWithUnboundedRoomKeepsContentSizedColumns() {
        GridLayout layout = WidgetLayout.grid().columns(Track.auto(), Track.weight(1)).spacing(4, 2).build();
        TestPanel panel = labelAndValue(100);

        layout.align(panel, UNBOUNDED, UNBOUNDED);

        assertEquals(5, panel.getWidgets().get(1).width);
    }

    // ---- defaults and panels

    @Test
    void layoutsWithoutConstraintSupportIgnoreTheRoom() {
        WidgetLayout custom = p -> 42;
        TestPanel panel = new TestPanel(10, 10);

        assertEquals(42, custom.align(panel, 5, 6));
        assertEquals(42, custom.align(panel, UNBOUNDED, UNBOUNDED));
    }

    @Test
    void legacyLayoutsBehaveTheSameWithOrWithoutRoom() {
        TestPanel a = new TestPanel(50, 50);
        TestPanel b = new TestPanel(50, 50);
        for (TestPanel p : new TestPanel[]{a, b}) {
            child(p, 10, 10);
            child(p, 30, 20);
        }
        WidgetLayout.Vertical layout = new WidgetLayout.Vertical(2, 1, 3);

        int plain = layout.align(a);
        int roomy = layout.align(b, 5, 6);

        assertEquals(plain, roomy);
        assertSamePlacement(a, b);
    }

    @Test
    void panelAlignWithRoomRecordsThePaddingForContentExtents() {
        TestPanel panel = new TestPanel(100, 100);
        child(panel, 10, 10);
        child(panel, 10, 12);

        int extent = panel.align(WidgetLayout.vertical().padding(new Insets(2, 0, 3, 0)).build(), UNBOUNDED, UNBOUNDED);

        assertEquals(27, extent);
        assertEquals(extent, panel.getContentHeight());
    }
}
