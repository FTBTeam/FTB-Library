package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PanelLayoutTest {
    private static final class ManagedPanel extends Panel {
        private final WidgetLayout layout;

        ManagedPanel(@Nullable Panel parent, WidgetLayout layout) {
            super(parent);
            this.layout = layout;
        }

        @Override
        protected WidgetLayout getLayout() {
            return layout;
        }

        @Override
        public void addWidgets() {
        }
    }

    private static final class LegacyPanel extends Panel {
        int alignCalls = 0;

        LegacyPanel() {
            super(null);
        }

        @Override
        public void addWidgets() {
        }

        @Override
        public void alignWidgets() {
            alignCalls++;
        }
    }

    private static final LayoutHints FILL = LayoutHints.DEFAULT.resize();

    @Test
    void managedPanelLaysOutByDefault() {
        ManagedPanel panel = new ManagedPanel(null, WidgetLayout.vertical().spacing(2).build());
        panel.setSize(50, 100);
        Widget a = new Widget(panel);
        a.setSize(10, 10);
        panel.add(a);
        Widget b = new Widget(panel);
        b.setSize(10, 10);
        panel.add(b);

        panel.alignWidgets();

        assertEquals(0, a.posY);
        assertEquals(12, b.posY);
    }

    @Test
    void resizingNestedPanelsFollowTheirParentTopDown() {
        ManagedPanel outer = new ManagedPanel(null, WidgetLayout.vertical().build());
        ManagedPanel inner = new ManagedPanel(outer, WidgetLayout.horizontal().build());
        Widget leaf = new Widget(inner);
        leaf.setSize(5, 5);
        leaf.setLayoutHints(LayoutHints.DEFAULT.withHFlex(Flex.RESIZE).withVFlex(Flex.RESIZE));
        inner.add(leaf);
        inner.setLayoutHints(FILL);
        outer.add(inner);

        outer.setSize(100, 100);
        outer.alignWidgets();

        assertEquals(100, inner.width);
        assertEquals(100, inner.height);
        assertEquals(100, leaf.width);
        assertEquals(100, leaf.height);

        outer.setSize(200, 50);
        outer.validateLayout();

        assertEquals(200, inner.width);
        assertEquals(50, inner.height);
        assertEquals(200, leaf.width);
        assertEquals(50, leaf.height);
    }

    @Test
    void validateDoesNothingWhenSizeIsUnchanged() {
        ManagedPanel panel = new ManagedPanel(null, WidgetLayout.vertical().build());
        panel.setSize(50, 50);
        Widget w = new Widget(panel);
        w.setSize(10, 10);
        panel.add(w);
        panel.alignWidgets();
        panel.validateLayout();

        w.setY(33);
        panel.setSize(50, 50); // same size: nothing becomes stale
        panel.validateLayout();

        assertEquals(33, w.posY);
    }

    @Test
    void invalidateForcesRelayout() {
        ManagedPanel panel = new ManagedPanel(null, WidgetLayout.vertical().build());
        panel.setSize(50, 50);
        Widget w = new Widget(panel);
        w.setSize(10, 10);
        panel.add(w);
        panel.validateLayout();
        w.setY(33);

        panel.invalidateLayout();
        panel.validateLayout();

        assertEquals(0, w.posY);
    }

    @Test
    void panelsWithoutALayoutAreNeverTouched() {
        LegacyPanel panel = new LegacyPanel();
        Widget w = new Widget(panel);
        w.setPosAndSize(7, 8, 9, 10);
        panel.add(w);

        panel.setSize(100, 100);
        panel.validateLayout();
        panel.invalidateLayout();
        panel.validateLayout();

        assertEquals(0, panel.alignCalls);
        assertEquals(7, w.posX);
        assertEquals(8, w.posY);
    }

    @Test
    void resizedChildOfLegacyPanelIsRevalidatedOnItsOwn() {
        LegacyPanel outer = new LegacyPanel();
        ManagedPanel inner = new ManagedPanel(outer, WidgetLayout.vertical().build());
        Widget leaf = new Widget(inner);
        leaf.setSize(5, 5);
        leaf.setLayoutHints(LayoutHints.DEFAULT.withVFlex(Flex.RESIZE));
        inner.add(leaf);
        outer.add(inner);

        inner.setSize(30, 60); // what a hand-written alignWidgets would do
        inner.validateLayout(); // what Panel#draw does before drawing

        assertEquals(60, leaf.height);
    }
}
