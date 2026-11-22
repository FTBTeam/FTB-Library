package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.ui.layout.LayoutHints;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Convenience extension of {@link Panel} to allow quick creation of a panel which uses a managed layout
 * (see {@link WidgetLayout} and {@link Panel#getLayout()}), and addition of widgets to that panel.
 */
public class ManagedLayoutPanel extends Panel {
    private final WidgetLayout layout;
    private final Consumer<ManagedLayoutPanel> widgetAdder;

    /**
     * Create a new panel with a managed layout.
     *
     * @param parent the parent panel
     * @param layout the layout this panel will use
     * @param widgetAdder consumer object to add widgets to this panel without needing to override {@link Panel#addWidgets()}
     */
    public ManagedLayoutPanel(Panel parent, WidgetLayout layout, Consumer<ManagedLayoutPanel> widgetAdder) {
        super(parent);
        this.layout = layout;
        this.widgetAdder = widgetAdder;
    }

    @Override
    public void addWidgets() {
        widgetAdder.accept(this);
    }

    @Override
    protected @Nullable WidgetLayout getLayout() {
        return layout;
    }

    public ManagedLayoutPanel withLayoutHints(LayoutHints hints) {
        setLayoutHints(hints);
        return this;
    }

    public ManagedLayoutPanel fitContent(boolean w, boolean h) {
        setFitContent(w, h);
        return this;
    }
}
