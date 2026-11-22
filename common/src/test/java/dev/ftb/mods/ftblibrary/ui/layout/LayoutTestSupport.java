package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

/**
 * Plain panels and widgets for layout tests; none of them touch Minecraft.
 */
final class LayoutTestSupport {
    private LayoutTestSupport() {
    }

    static final class TestPanel extends Panel {
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

    static Widget child(Panel parent, int w, int h) {
        Widget widget = new Widget(parent);
        widget.setSize(w, h);
        parent.add(widget);
        return widget;
    }

    static Widget hinted(Panel parent, int w, int h, LayoutHints hints) {
        Widget widget = child(parent, w, h);
        widget.setLayoutHints(hints);
        return widget;
    }

    /** A panel that lays itself out with whatever layout it is given (none if null). */
    static final class ManagedPanel extends Panel {
        private final WidgetLayout layout;

        ManagedPanel(Panel parent, WidgetLayout layout) {
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
}
