package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinearLayoutTest {
    private static final class TestPanel extends Panel {
        TestPanel(int w, int h) {
            super(null);
            setSize(w, h);
        }

        @Override
        public void addWidgets() {
        }

        @Override
        public void alignWidgets() {
        }
    }

    private static Widget child(Panel parent, int w, int h) {
        Widget widget = new Widget(parent);
        widget.setSize(w, h);
        parent.add(widget);
        return widget;
    }

    private static TestPanel panelWith(int panelW, int panelH, int... sizes) {
        TestPanel panel = new TestPanel(panelW, panelH);
        for (int i = 0; i < sizes.length; i += 2) {
            child(panel, sizes[i], sizes[i + 1]);
        }
        return panel;
    }

    private static final int[][] SIZE_SETS = {
            {},
            {10, 20},
            {10, 20, 30, 5},
            {7, 7, 7, 7, 7, 7, 7, 7},
            {0, 0, 12, 12}
    };
    private static final int[][] PRE_SPACING_POST = {{0, 0, 0}, {2, 1, 1}, {5, 5, 0}, {3, 3, 10}, {0, 4, 0}};

    // The new layout must reproduce the older Vertical/Horizontal layouts exactly when no cross-axis option is used.

    @Test
    void verticalMatchesLegacyLayout() {
        for (int[] sizes : SIZE_SETS) {
            for (int[] p : PRE_SPACING_POST) {
                TestPanel legacy = panelWith(50, 50, sizes);
                TestPanel modern = panelWith(50, 50, sizes);

                int expected = legacy.align(new WidgetLayout.Vertical(p[0], p[1], p[2]));
                int actual = modern.align(WidgetLayout.vertical()
                        .padding(new Insets(p[0], 0, p[2], 0)).spacing(p[1]).build());

                assertEquals(expected, actual);
                assertSamePlacement(legacy, modern);
            }
        }
    }

    @Test
    void horizontalMatchesLegacyLayout() {
        for (int[] sizes : SIZE_SETS) {
            for (int[] p : PRE_SPACING_POST) {
                TestPanel legacy = panelWith(50, 50, sizes);
                TestPanel modern = panelWith(50, 50, sizes);

                int expected = legacy.align(new WidgetLayout.Horizontal(p[0], p[1], p[2]));
                int actual = modern.align(WidgetLayout.horizontal()
                        .padding(new Insets(0, p[2], 0, p[0])).spacing(p[1]).build());

                assertEquals(expected, actual);
                assertSamePlacement(legacy, modern);
            }
        }
    }

    private static void assertSamePlacement(Panel expected, Panel actual) {
        assertEquals(expected.getWidgets().size(), actual.getWidgets().size());
        for (int i = 0; i < expected.getWidgets().size(); i++) {
            Widget e = expected.getWidgets().get(i);
            Widget a = actual.getWidgets().get(i);
            assertEquals(e.posX, a.posX, "posX of widget " + i);
            assertEquals(e.posY, a.posY, "posY of widget " + i);
            assertEquals(e.width, a.width, "width of widget " + i);
            assertEquals(e.height, a.height, "height of widget " + i);
        }
        if (!expected.getWidgets().isEmpty()) {
            assertEquals(expected.getContentWidth(), actual.getContentWidth());
            assertEquals(expected.getContentHeight(), actual.getContentHeight());
        }
    }

    @Test
    void fillCrossMatchesTheManualPatternUsedByButtonLists() {
        // what ButtonPanel used to do by hand after a Vertical layout
        TestPanel legacy = panelWith(100, 80, 5, 10, 60, 12, 100, 8);
        legacy.align(new WidgetLayout.Vertical(4, 2, 4));
        legacy.getWidgets().forEach(w -> {
            w.setX(3);
            w.setWidth(legacy.width - 3 * 2);
        });

        TestPanel modern = panelWith(100, 80, 5, 10, 60, 12, 100, 8);
        int expected = legacy.getContentHeight();
        modern.align(WidgetLayout.vertical().padding(new Insets(4, 3, 4, 3)).spacing(2).fillCross().build());

        for (int i = 0; i < 3; i++) {
            Widget e = legacy.getWidgets().get(i);
            Widget a = modern.getWidgets().get(i);
            assertEquals(e.posX, a.posX);
            assertEquals(e.posY, a.posY);
            assertEquals(e.width, a.width);
            assertEquals(e.height, a.height);
        }
        assertEquals(expected, modern.getContentHeight());
        // the only difference: the new layout also counts the side insets, so content spans the full panel width
        assertEquals(100 - 3 * 2, legacy.getContentWidth());
        assertEquals(100, modern.getContentWidth());
    }

    @Test
    void defaultsLeaveCrossAxisAlone() {
        TestPanel panel = panelWith(100, 100, 10, 10, 20, 10);
        panel.getWidgets().get(0).setX(7);
        panel.getWidgets().get(1).setX(9);

        panel.align(WidgetLayout.vertical().spacing(2).build());

        assertEquals(7, panel.getWidgets().get(0).posX);
        assertEquals(9, panel.getWidgets().get(1).posX);
        assertEquals(10 + 2, panel.getWidgets().get(1).posY);
    }

    @Test
    void verticalFillWithInsets() {
        TestPanel panel = panelWith(100, 100, 10, 10, 30, 10);

        panel.align(WidgetLayout.vertical().padding(new Insets(0, 6, 0, 4)).fillCross().build());

        for (Widget w : panel.getWidgets()) {
            assertEquals(4, w.posX);
            assertEquals(90, w.width);
        }
    }

    @Test
    void verticalCenterAndEnd() {
        TestPanel center = panelWith(100, 100, 10, 10, 30, 10);
        center.align(WidgetLayout.vertical().crossAlign(Align.CENTER).build());
        assertEquals(45, center.getWidgets().get(0).posX);
        assertEquals(35, center.getWidgets().get(1).posX);

        TestPanel end = panelWith(100, 100, 10, 10, 30, 10);
        end.align(WidgetLayout.vertical().crossAlign(Align.END).build());
        assertEquals(90, end.getWidgets().get(0).posX);
        assertEquals(70, end.getWidgets().get(1).posX);
    }

    @Test
    void endRespectsTrailingInset() {
        TestPanel panel = panelWith(100, 100, 10, 10);
        panel.align(WidgetLayout.vertical().padding(new Insets(0, 5, 0, 3)).crossAlign(Align.END).build());
        // available = 100 - 8 = 92, offset = 82, plus leading inset 3
        assertEquals(85, panel.getWidgets().getFirst().posX);
    }

    @Test
    void unsizedPanelFallsBackToWidestWidget() {
        TestPanel panel = panelWith(0, 0, 10, 10, 30, 10);

        panel.align(WidgetLayout.vertical().crossAlign(Align.CENTER).build());

        assertEquals(10, panel.getWidgets().get(0).posX);
        assertEquals(0, panel.getWidgets().get(1).posX);

        TestPanel fill = panelWith(0, 0, 10, 10, 30, 10);
        fill.align(WidgetLayout.vertical().padding(Insets.of(0, 2)).fillCross().build());
        assertEquals(30, fill.getWidgets().getFirst().width);
        assertEquals(2, fill.getWidgets().getFirst().posX);
    }

    @Test
    void explicitCrossSizeWins() {
        TestPanel panel = panelWith(100, 100, 10, 10);

        panel.align(WidgetLayout.vertical().crossSize(50).crossAlign(Align.END).build());

        assertEquals(40, panel.getWidgets().getFirst().posX);
    }

    @Test
    void oversizedWidgetIsPinnedToStart() {
        TestPanel panel = panelWith(20, 100, 50, 10);

        panel.align(WidgetLayout.vertical().crossAlign(Align.CENTER).build());

        assertEquals(0, panel.getWidgets().getFirst().posX);
    }

    @Test
    void horizontalFillAndAlignUseHeight() {
        TestPanel fill = panelWith(100, 40, 10, 10, 10, 20);
        fill.align(WidgetLayout.horizontal().padding(Insets.of(3, 0)).fillCross().build());
        for (Widget w : fill.getWidgets()) {
            assertEquals(3, w.posY);
            assertEquals(34, w.height);
        }

        TestPanel center = panelWith(100, 40, 10, 10, 10, 20);
        center.align(WidgetLayout.horizontal().crossAlign(Align.CENTER).build());
        assertEquals(15, center.getWidgets().get(0).posY);
        assertEquals(10, center.getWidgets().get(1).posY);
    }

    @Test
    void measureCrossIsWidestPlusInsets() {
        TestPanel v = panelWith(0, 0, 10, 10, 30, 10);
        assertEquals(30 + 6, WidgetLayout.vertical().padding(Insets.of(0, 3)).build().measureCross(v));

        TestPanel h = panelWith(0, 0, 10, 10, 10, 25);
        assertEquals(25 + 4, WidgetLayout.horizontal().padding(Insets.of(2, 0)).build().measureCross(h));

        assertEquals(0, WidgetLayout.vertical().build().measureCross(new TestPanel(0, 0)));
    }

    @Test
    void contentExtentsMatchLayoutResult() {
        TestPanel panel = panelWith(100, 100, 10, 10, 30, 12);

        int height = panel.align(WidgetLayout.vertical().padding(new Insets(2, 5, 3, 4)).spacing(1).fillCross().build());

        assertEquals(height, panel.getContentHeight());
        assertEquals(100, panel.getContentWidth()); // 91 wide widgets starting at 4, plus 4 + 5 of insets
    }

    @Test
    void layoutIsReusableAcrossPanels() {
        LinearLayout shared = WidgetLayout.vertical().padding(Insets.of(2)).spacing(3).fillCross().build();

        TestPanel a = panelWith(50, 50, 10, 10, 10, 10);
        TestPanel b = panelWith(80, 80, 10, 10);
        int ra = a.align(shared);
        int rb = b.align(shared);

        assertEquals(2 + 10 + 3 + 10 + 2, ra);
        assertEquals(2 + 10 + 2, rb);
        assertEquals(46, a.getWidgets().getFirst().width);
        assertEquals(76, b.getWidgets().getFirst().width);
    }

    // ---- hints

    private static Widget hinted(Panel parent, int w, int h, LayoutHints hints) {
        Widget widget = child(parent, w, h);
        widget.setLayoutHints(hints);
        return widget;
    }

    private static final LayoutHints RESIZE_V = LayoutHints.DEFAULT.withVFlex(Flex.RESIZE);

    @Test
    void resizeChildTakesLeftoverMainSpace() {
        TestPanel panel = new TestPanel(40, 100);
        Widget top = child(panel, 40, 10);
        Widget mid = hinted(panel, 40, 0, RESIZE_V);
        Widget bottom = child(panel, 40, 10);

        int extent = panel.align(WidgetLayout.vertical().padding(new Insets(2, 0, 3, 0)).spacing(5).build());

        assertEquals(100 - 5 - 20 - 10, mid.height); // insets, children, two gaps
        assertEquals(2, top.posY);
        assertEquals(2 + 10 + 5, mid.posY);
        assertEquals(100 - 3 - 10, bottom.posY);
        assertEquals(100, extent);
    }

    @Test
    void resizeChildrenShareByWeight() {
        TestPanel panel = new TestPanel(40, 100);
        child(panel, 40, 10);
        Widget a = hinted(panel, 40, 0, RESIZE_V);
        Widget b = hinted(panel, 40, 0, RESIZE_V.withWeight(2));

        panel.align(WidgetLayout.vertical().build());

        assertEquals(30, a.height);
        assertEquals(60, b.height);
        assertEquals(10 + 30, b.posY);
    }

    @Test
    void resizeRespectsMinAndMax() {
        TestPanel panel = new TestPanel(40, 100);
        Widget a = hinted(panel, 40, 0, RESIZE_V.withMaxSize(LayoutHints.UNBOUNDED, 20));
        Widget b = hinted(panel, 40, 0, RESIZE_V);

        panel.align(WidgetLayout.vertical().build());

        assertEquals(20, a.height);
        assertEquals(80, b.height);
        assertEquals(20, b.posY);
    }

    @Test
    void resizeSplitsPixelsExactlyAcrossThree() {
        TestPanel panel = new TestPanel(40, 100);
        Widget a = hinted(panel, 40, 0, RESIZE_V);
        Widget b = hinted(panel, 40, 0, RESIZE_V);
        Widget c = hinted(panel, 40, 0, RESIZE_V);

        int extent = panel.align(WidgetLayout.vertical().build());

        assertEquals(100, a.height + b.height + c.height);
        assertEquals(100, extent);
    }

    @Test
    void resizeWithoutPanelSizeKeepsCurrentSizeClamped() {
        TestPanel panel = new TestPanel(40, 0);
        Widget a = hinted(panel, 40, 15, RESIZE_V);
        Widget b = hinted(panel, 40, 5, RESIZE_V.withMinSize(0, 8));

        int extent = panel.align(WidgetLayout.vertical().build());

        assertEquals(15, a.height);
        assertEquals(8, b.height);
        assertEquals(23, extent);
    }

    @Test
    void unboundedMainKeepsResizeChildrenAtCurrentSize() {
        TestPanel panel = new TestPanel(40, 100);
        Widget a = hinted(panel, 40, 15, RESIZE_V);

        panel.align(WidgetLayout.vertical().unboundedMain().build());

        assertEquals(15, a.height);
    }

    @Test
    void resizeChildrenShrinkToMinimumWhenFixedContentOverflows() {
        TestPanel panel = new TestPanel(40, 30);
        child(panel, 40, 20);
        Widget a = hinted(panel, 40, 50, RESIZE_V.withMinSize(0, 4));
        child(panel, 40, 20);

        int extent = panel.align(WidgetLayout.vertical().build());

        assertEquals(4, a.height);
        assertEquals(44, extent); // overflows the panel; nothing else shrinks
    }

    @Test
    void horizontalResizeUsesWidth() {
        TestPanel panel = new TestPanel(100, 20);
        child(panel, 10, 20);
        Widget fill = hinted(panel, 0, 20, LayoutHints.DEFAULT.withHFlex(Flex.RESIZE));

        panel.align(WidgetLayout.horizontal().spacing(2).build());

        assertEquals(100 - 10 - 2, fill.width);
        assertEquals(12, fill.posX);
    }

    @Test
    void marginsAddSpaceOnBothAxes() {
        TestPanel panel = new TestPanel(100, 100);
        Widget a = hinted(panel, 20, 10, LayoutHints.DEFAULT.withMargin(new Insets(3, 4, 2, 5)));
        Widget b = child(panel, 20, 10);

        panel.align(WidgetLayout.vertical().spacing(1).build());

        assertEquals(3, a.posY);
        assertEquals(5, a.posX);
        assertEquals(3 + 10 + 2 + 1, b.posY);
    }

    @Test
    void marginsReduceFillAndMeasure() {
        TestPanel panel = new TestPanel(100, 100);
        Widget a = hinted(panel, 20, 10, LayoutHints.DEFAULT.withMargin(Insets.of(0, 6)));

        LinearLayout layout = WidgetLayout.vertical().fillCross().build();
        panel.align(layout);

        assertEquals(88, a.width);
        assertEquals(6, a.posX);
        assertEquals(88 + 12, layout.measureCross(panel));
    }

    @Test
    void crossResizeHintFillsOnlyThatWidget() {
        TestPanel panel = new TestPanel(100, 100);
        Widget fill = hinted(panel, 10, 10, LayoutHints.DEFAULT.withHFlex(Flex.RESIZE).withMinSize(0, 0).withMaxSize(60, LayoutHints.UNBOUNDED));
        Widget plain = child(panel, 10, 10);

        panel.align(WidgetLayout.vertical().build());

        assertEquals(60, fill.width);
        assertEquals(10, plain.width);
    }

    @Test
    void childAlignHintOverridesLayoutAlignment() {
        TestPanel panel = new TestPanel(100, 100);
        Widget end = hinted(panel, 10, 10, LayoutHints.DEFAULT.withHAlign(Align.END));
        Widget inherits = child(panel, 10, 10);

        panel.align(WidgetLayout.vertical().crossAlign(Align.CENTER).build());

        assertEquals(90, end.posX);
        assertEquals(45, inherits.posX);
    }

    @Test
    void hintsOnHorizontalLayoutUseVerticalAlignment() {
        TestPanel panel = new TestPanel(100, 40);
        Widget w = hinted(panel, 10, 10, LayoutHints.DEFAULT.withVAlign(Align.END));

        panel.align(WidgetLayout.horizontal().build());

        assertEquals(30, w.posY);
    }

    @Test
    void layoutHintsAreNormalised() {
        LayoutHints h = new LayoutHints(Flex.INTRINSIC, Flex.INTRINSIC, null, null, 0, -4, -4, 2, 2, Insets.ZERO, 0, -3);
        assertEquals(Flex.INTRINSIC, h.hFlex());
        assertEquals(1, h.weight());
        assertEquals(0, h.minWidth());
        assertEquals(Insets.ZERO, h.margin());
        assertEquals(1, h.colSpan());
        assertEquals(1, h.rowSpan());
        assertEquals(10, LayoutHints.DEFAULT.withMinSize(10, 10).withMaxSize(3, 3).maxWidth());
    }

    @Test
    void layoutHintsRejectMissingFlexAndMargin() {
        assertThrows(NullPointerException.class, () -> LayoutHints.DEFAULT.withHFlex(null));
        assertThrows(NullPointerException.class, () -> LayoutHints.DEFAULT.withVFlex(null));
        assertThrows(NullPointerException.class, () -> LayoutHints.DEFAULT.withMargin(null));
    }

    @Test
    void widgetsDefaultToDefaultHints() {
        TestPanel panel = new TestPanel(0, 0);
        Widget w = child(panel, 1, 1);
        assertEquals(LayoutHints.DEFAULT, w.getLayoutHints());
        w.setLayoutHints(null);
        assertEquals(LayoutHints.DEFAULT, w.getLayoutHints());
    }
}
