package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.TestPanel;
import org.junit.jupiter.api.Test;

import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.child;
import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.hinted;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FlowLayoutTest {
    private static TestPanel threeWide(int panelWidth) {
        TestPanel panel = new TestPanel(panelWidth, 0);
        child(panel, 30, 10);
        child(panel, 30, 10);
        child(panel, 30, 10);
        return panel;
    }

    @Test
    void wrapsWhenLineIsFull() {
        TestPanel panel = threeWide(70);

        int height = panel.align(WidgetLayout.flow().spacing(5, 2).build());

        var w = panel.getWidgets();
        assertEquals(0, w.get(0).posX);
        assertEquals(35, w.get(1).posX);
        assertEquals(0, w.get(1).posY);
        assertEquals(0, w.get(2).posX);
        assertEquals(12, w.get(2).posY);
        assertEquals(22, height);
    }

    @Test
    void doesNotWrapWithoutAWidth() {
        TestPanel panel = threeWide(0);

        int height = panel.align(WidgetLayout.flow().spacing(5, 2).build());

        assertEquals(0, panel.getWidgets().get(0).posX);
        assertEquals(35, panel.getWidgets().get(1).posX);
        assertEquals(70, panel.getWidgets().get(2).posX);
        assertEquals(10, height);
    }

    @Test
    void insetsShrinkTheLineAndOffsetTheContent() {
        TestPanel panel = threeWide(80);

        // 80 - 2*5 = 70 available, so the third widget wraps
        int height = panel.align(WidgetLayout.flow().padding(5).spacing(5, 2).build());

        var w = panel.getWidgets();
        assertEquals(5, w.get(0).posX);
        assertEquals(5, w.get(0).posY);
        assertEquals(5, w.get(2).posX);
        assertEquals(5 + 10 + 2, w.get(2).posY);
        assertEquals(5 + 10 + 2 + 10 + 5, height);
        assertEquals(height, panel.getContentHeight());
    }

    @Test
    void linesCanBeCentredOrRightAligned() {
        TestPanel centred = threeWide(70);
        centred.align(WidgetLayout.flow().spacing(5, 2).lineAlign(Align.CENTER).build());
        // first line is 65 wide in 70, second is 30 wide
        assertEquals(2, centred.getWidgets().get(0).posX);
        assertEquals(37, centred.getWidgets().get(1).posX);
        assertEquals(20, centred.getWidgets().get(2).posX);

        TestPanel right = threeWide(70);
        right.align(WidgetLayout.flow().spacing(5, 2).lineAlign(Align.END).build());
        assertEquals(5, right.getWidgets().get(0).posX);
        assertEquals(40, right.getWidgets().get(2).posX);
    }

    @Test
    void itemsOfDifferentHeightsAlignWithinTheirLine() {
        for (Align align : Align.values()) {
            TestPanel panel = new TestPanel(100, 0);
            Widget small = child(panel, 10, 10);
            child(panel, 10, 20);

            int height = panel.align(WidgetLayout.flow().itemAlign(align).build());

            assertEquals(20, height);
            assertEquals(switch (align) {
                case START -> 0;
                case CENTER -> 5;
                case END -> 10;
            }, small.posY);
        }
    }

    @Test
    void marginsAreRespected() {
        TestPanel panel = new TestPanel(100, 0);
        Widget w = hinted(panel, 10, 10, LayoutHints.DEFAULT.withMargin(new Insets(1, 2, 3, 4)));
        Widget next = child(panel, 10, 10);

        int height = panel.align(WidgetLayout.flow().build());

        assertEquals(4, w.posX);
        assertEquals(1, w.posY);
        assertEquals(4 + 10 + 2, next.posX); // previous footprint is 16 wide
        assertEquals(14, height);
    }

    @Test
    void widgetWiderThanTheLineGetsItsOwn() {
        TestPanel panel = new TestPanel(50, 0);
        child(panel, 100, 10);
        Widget small = child(panel, 10, 10);

        int height = panel.align(WidgetLayout.flow().spacing(0, 3).build());

        assertEquals(0, small.posX);
        assertEquals(13, small.posY);
        assertEquals(23, height);
    }

    @Test
    void emptyFlowHasOnlyInsets() {
        assertEquals(8, new TestPanel(50, 50).align(WidgetLayout.flow().padding(Insets.of(4, 1)).build()));
    }

    @Test
    void layoutIsReusable() {
        FlowLayout shared = WidgetLayout.flow().spacing(5, 2).build();
        TestPanel a = threeWide(70);
        TestPanel b = threeWide(0);

        assertEquals(22, a.align(shared));
        assertEquals(10, b.align(shared));
    }
}
