package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.TestPanel;
import org.junit.jupiter.api.Test;

import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.child;
import static dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.hinted;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StackLayoutTest {
    @Test
    void resizeFillsThePanel() {
        TestPanel panel = new TestPanel(100, 50);
        Widget bg = hinted(panel, 1, 1, LayoutHints.DEFAULT.resize());

        panel.align(WidgetLayout.stack().build());

        assertEquals(0, bg.posX);
        assertEquals(0, bg.posY);
        assertEquals(100, bg.width);
        assertEquals(50, bg.height);
    }

    @Test
    void insetsAndMarginsShrinkTheFill() {
        TestPanel panel = new TestPanel(100, 50);
        Widget bg = hinted(panel, 1, 1, LayoutHints.DEFAULT.resize().withMargin(Insets.of(2)));

        panel.align(WidgetLayout.stack().padding(3).build());

        assertEquals(100 - 6 - 4, bg.width);
        assertEquals(50 - 6 - 4, bg.height);
        assertEquals(5, bg.posX);
        assertEquals(5, bg.posY);
    }

    @Test
    void anchorsToACornerWithAMargin() {
        TestPanel panel = new TestPanel(100, 50);
        Widget button = hinted(panel, 20, 10, LayoutHints.DEFAULT
                .withHAlign(Align.END).withVAlign(Align.END).withMargin(Insets.of(4)));

        panel.align(WidgetLayout.stack().build());

        assertEquals(4 + (100 - 8 - 20), button.posX);
        assertEquals(4 + (50 - 8 - 10), button.posY);
    }

    @Test
    void layoutDefaultAlignmentAppliesToUnhintedWidgets() {
        TestPanel panel = new TestPanel(100, 50);
        Widget centred = child(panel, 10, 10);
        Widget top = hinted(panel, 10, 10, LayoutHints.DEFAULT.withVAlign(Align.START));

        panel.align(WidgetLayout.stack().align(Align.CENTER, Align.CENTER).build());

        assertEquals(45, centred.posX);
        assertEquals(20, centred.posY);
        assertEquals(45, top.posX);
        assertEquals(0, top.posY);
    }

    @Test
    void panelWithoutASizeFitsTheLargestWidget() {
        TestPanel panel = new TestPanel(0, 0);
        Widget small = child(panel, 20, 10);
        child(panel, 30, 40);

        int height = panel.align(WidgetLayout.stack().padding(5).align(Align.CENTER, Align.CENTER).build());

        assertEquals(40 + 10, height);
        assertEquals(5 + 5, small.posX);
        assertEquals(5 + 15, small.posY);
    }

    @Test
    void resizeRespectsMaxSize() {
        TestPanel panel = new TestPanel(100, 50);
        Widget w = hinted(panel, 1, 1, LayoutHints.DEFAULT.withHFlex(Flex.RESIZE).withMaxSize(60, LayoutHints.UNBOUNDED));

        panel.align(WidgetLayout.stack().build());

        assertEquals(60, w.width);
        assertEquals(1, w.height);
    }

    @Test
    void paddingIsReported() {
        assertEquals(new WidgetLayout.Padding(6, 8), WidgetLayout.stack().padding(Insets.of(3, 4)).build().getLayoutPadding());
    }
}
