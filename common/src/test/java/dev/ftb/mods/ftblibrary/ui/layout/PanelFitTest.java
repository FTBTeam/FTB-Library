package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.layout.GridLayout.Track;
import dev.ftb.mods.ftblibrary.ui.layout.LayoutTestSupport.ManagedPanel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PanelFitTest {
    private static Widget add(ManagedPanel panel, int w, int h) {
        Widget widget = new Widget(panel);
        widget.setSize(w, h);
        panel.add(widget);
        return widget;
    }

    private static Widget add(ManagedPanel panel, int w, int h, LayoutHints hints) {
        Widget widget = add(panel, w, h);
        widget.setLayoutHints(hints);
        return widget;
    }

    private static ManagedPanel fitting(WidgetLayout layout, int w, int h, boolean fitWidth, boolean fitHeight) {
        ManagedPanel panel = new ManagedPanel(null, layout);
        panel.setSize(w, h);
        panel.setFitContent(fitWidth, fitHeight);
        return panel;
    }

    @Test
    void fitIsOffByDefault() {
        ManagedPanel panel = new ManagedPanel(null, WidgetLayout.vertical().build());
        assertFalse(panel.isFitContentWidth());
        assertFalse(panel.isFitContentHeight());
        assertFalse(panel.isFittingContent(true));

        panel.setFitContent(true, false);
        assertTrue(panel.isFitContentWidth());
        assertFalse(panel.isFitContentHeight());
        assertTrue(panel.isFittingContent(true));
        assertFalse(panel.isFittingContent(false));
    }

    @Test
    void fitsHeightOnlyAndKeepsTheWidth() {
        ManagedPanel panel = fitting(WidgetLayout.vertical().padding(3).spacing(2).build(), 50, 0, false, true);
        add(panel, 10, 10);
        add(panel, 10, 20);

        panel.alignWidgets();

        assertEquals(50, panel.width);
        assertEquals(3 + 10 + 2 + 20 + 3, panel.height);
    }

    @Test
    void fitsBothAxesIgnoringAStaleSize() {
        ManagedPanel panel = fitting(WidgetLayout.vertical().padding(Insets.of(3, 4)).build(), 100, 100, true, true);
        add(panel, 10, 10);
        add(panel, 30, 20);

        panel.alignWidgets();

        assertEquals(30 + 8, panel.width);
        assertEquals(3 + 10 + 20 + 3, panel.height);
    }

    @Test
    void resizingChildrenKeepTheirSizeOnAFittedAxis() {
        ManagedPanel panel = fitting(WidgetLayout.vertical().build(), 100, 100, false, true);
        add(panel, 10, 10);
        Widget stretchy = add(panel, 10, 5, LayoutHints.DEFAULT.withVFlex(Flex.RESIZE));

        panel.alignWidgets();

        assertEquals(5, stretchy.height);
        assertEquals(15, panel.height);

        panel.invalidateLayout();
        panel.validateLayout();
        assertEquals(15, panel.height); // settled: refitting changes nothing
    }

    @Test
    void ownMinAndMaxSizeBoundTheFit() {
        ManagedPanel tall = fitting(WidgetLayout.vertical().build(), 50, 0, false, true);
        tall.setLayoutHints(LayoutHints.DEFAULT.withMinSize(0, 100));
        add(tall, 10, 20);
        tall.alignWidgets();
        assertEquals(100, tall.height);

        ManagedPanel capped = fitting(WidgetLayout.vertical().build(), 50, 0, false, true);
        capped.setLayoutHints(LayoutHints.DEFAULT.withMaxSize(LayoutHints.UNBOUNDED, 15));
        add(capped, 10, 20);
        capped.alignWidgets();
        assertEquals(15, capped.height);
    }

    @Test
    void heightFollowsWidthForWrappingContent() {
        ManagedPanel panel = fitting(WidgetLayout.flow().spacing(5, 2).build(), 70, 0, false, true);
        add(panel, 30, 10);
        add(panel, 30, 10);
        add(panel, 30, 10);

        panel.alignWidgets();
        assertEquals(22, panel.height);

        panel.setWidth(100);
        panel.validateLayout();
        assertEquals(10, panel.height);
    }

    @Test
    void fitsStackAndGridLayouts() {
        ManagedPanel stack = fitting(WidgetLayout.stack().padding(2).build(), 0, 0, true, true);
        add(stack, 20, 10);
        add(stack, 30, 40);
        stack.alignWidgets();
        assertEquals(34, stack.width);
        assertEquals(44, stack.height);

        ManagedPanel grid = fitting(WidgetLayout.grid().columns(Track.auto(), Track.weight(1)).spacing(1).build(), 0, 0, true, true);
        for (int i = 0; i < 4; i++) {
            add(grid, 10, 10);
        }
        grid.alignWidgets();
        assertEquals(21, grid.width);
        assertEquals(21, grid.height);
    }

    @Test
    void aChangeDeepInsideReachesTheTopAndMovesSiblings() {
        ManagedPanel outer = fitting(WidgetLayout.vertical().build(), 50, 0, false, true);
        ManagedPanel inner = new ManagedPanel(outer, WidgetLayout.vertical().build());
        inner.setSize(50, 0);
        inner.setFitContent(false, true);
        add(inner, 10, 20);
        outer.add(inner);
        Widget footer = add(outer, 10, 10);

        outer.alignWidgets();

        assertEquals(20, inner.height);
        assertEquals(20, footer.posY);
        assertEquals(30, outer.height);

        add(inner, 10, 30); // content of the inner panel grows
        outer.validateLayout();

        assertEquals(50, inner.height);
        assertEquals(50, footer.posY);
        assertEquals(60, outer.height);
    }

    @Test
    void resizingAWidgetInsideRefitsThePanel() {
        ManagedPanel panel = fitting(WidgetLayout.vertical().build(), 50, 0, false, true);
        Widget child = add(panel, 10, 10);
        panel.alignWidgets();
        assertEquals(10, panel.height);

        child.setHeight(40);
        panel.validateLayout();

        assertEquals(40, panel.height);
    }

    @Test
    void changingLayoutHintsRelaysOutTheParent() {
        ManagedPanel panel = fitting(WidgetLayout.vertical().build(), 50, 0, false, true);
        Widget child = add(panel, 10, 10);
        panel.alignWidgets();

        child.setLayoutHints(LayoutHints.DEFAULT.withMargin(Insets.of(5, 0)));
        panel.validateLayout();

        assertEquals(20, panel.height);
        assertEquals(5, child.posY);
    }

    @Test
    void aParentNeverStretchesAFittingChildAlongTheFittedAxes() {
        ManagedPanel outer = new ManagedPanel(null, WidgetLayout.vertical().fillCross().build());
        outer.setSize(100, 100);
        ManagedPanel inner = new ManagedPanel(outer, WidgetLayout.vertical().build());
        inner.setFitContent(true, true);
        inner.setLayoutHints(LayoutHints.DEFAULT.resize());
        add(inner, 20, 30);
        outer.add(inner);
        Widget footer = add(outer, 10, 10);

        outer.alignWidgets();

        assertEquals(20, inner.width);
        assertEquals(30, inner.height);
        assertEquals(30, footer.posY);
    }

    @Test
    void aParentStillStretchesAChildOnTheAxisItDoesNotFit() {
        ManagedPanel outer = new ManagedPanel(null, WidgetLayout.vertical().fillCross().build());
        outer.setSize(100, 100);
        ManagedPanel inner = new ManagedPanel(outer, WidgetLayout.vertical().build());
        inner.setFitContent(false, true);
        add(inner, 20, 30);
        outer.add(inner);

        outer.alignWidgets();

        assertEquals(100, inner.width);
        assertEquals(30, inner.height);
    }

    @Test
    void childHeightThatDependsOnItsWidthSettlesInTheParent() {
        ManagedPanel outer = new ManagedPanel(null, WidgetLayout.vertical().fillCross().build());
        outer.setSize(70, 0);
        ManagedPanel flow = new ManagedPanel(outer, WidgetLayout.flow().spacing(5, 2).build());
        flow.setFitContent(false, true);
        add(flow, 30, 10);
        add(flow, 30, 10);
        add(flow, 30, 10);
        outer.add(flow);
        Widget footer = add(outer, 10, 10);

        outer.alignWidgets();

        // stretched to 70 wide, the flow wraps to two lines (22 high) and the footer sits below that
        assertEquals(70, flow.width);
        assertEquals(22, flow.height);
        assertEquals(22, footer.posY);
    }

    @Test
    void layoutsThatCannotMeasureLeaveTheSizeAlone() {
        ManagedPanel panel = fitting(p -> 0, 50, 60, true, true);
        add(panel, 10, 10);

        panel.alignWidgets();

        assertEquals(50, panel.width);
        assertEquals(60, panel.height);
    }

    @Test
    void panelsWithoutALayoutAreNotFitted() {
        ManagedPanel panel = fitting(null, 50, 60, true, true);
        add(panel, 10, 10);

        panel.alignWidgets();
        panel.validateLayout();

        assertEquals(50, panel.width);
        assertEquals(60, panel.height);
    }

    @Test
    void fittingPanelsAreUnaffectedByTheNonFittingOnes() {
        // a plain managed panel next to a fitted one behaves exactly as before
        ManagedPanel panel = new ManagedPanel(null, WidgetLayout.vertical().spacing(2).build());
        panel.setSize(50, 100);
        Widget a = add(panel, 10, 10);
        Widget b = add(panel, 10, 10, LayoutHints.DEFAULT.withVFlex(Flex.RESIZE));

        panel.alignWidgets();

        assertEquals(0, a.posY);
        assertEquals(12, b.posY);
        assertEquals(88, b.height);
        assertEquals(100, panel.height);
    }

    /** A widget whose height flips every time a layout sets its width, so the layout never settles. */
    private static final class Restless extends Widget {
        int widthSets;

        Restless(ManagedPanel parent) {
            super(parent);
            setSize(10, 10);
        }

        @Override
        public void setWidth(int v) {
            widthSets++;
            super.setWidth(v);
            setHeight(height == 10 ? 20 : 10);
        }
    }

    @Test
    void layoutPassesAreCappedWhenTheContentNeverSettles() {
        ManagedPanel panel = fitting(WidgetLayout.vertical().fillCross().build(), 50, 0, false, true);
        Restless restless = new Restless(panel);
        restless.setLayoutHints(LayoutHints.DEFAULT.resize());
        panel.add(restless);
        restless.widthSets = 0;

        panel.alignWidgets();

        // Panel.MAX_LAYOUT_PASSES; the layout sets the width once a pass
        assertEquals(8, restless.widthSets);

        // the panel is not left stuck mid layout, so it can be laid out again
        restless.widthSets = 0;
        panel.alignWidgets();
        assertEquals(8, restless.widthSets);
    }
}
