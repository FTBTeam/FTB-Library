package dev.ftb.mods.ftblibrary.ui;


public class PanelScrollBar extends ScrollBar {
    private final Panel panel;

    public PanelScrollBar(Panel parent, Plane plane, Panel p) {
        super(parent, plane, 0);
        panel = p;
    }

    public PanelScrollBar(Panel parent, Panel panel) {
        this(parent, Plane.VERTICAL, panel);
    }

    public Panel getPanel() {
        return panel;
    }

    @Override
    public double getMinValue() {
        return 0;
    }

    @Override
    public void setMinValue(double min) {
    }

    @Override
    public double getMaxValue() {
        return plane.isVertical ? panel.getContentHeight() - panel.getHeight() : panel.getContentWidth() - panel.getWidth();
    }

    @Override
    public void setMaxValue(double max) {
        // max value is auto-calculated from panel dimensions; don't change it
        throw new UnsupportedOperationException("attempt to set max value of panel scrollbar");
    }

    @Override
    public double getScrollStep() {
        return panel.getScrollStep();
    }

    @Override
    public void setScrollStep(double s) {
        panel.setScrollStep(s);
    }

    @Override
    public int getScrollBarSize() {
        var max = getMaxValue();
        if (max <= 0) {
            return 0;
        }

        int size = plane.isVertical ?
                (int) (panel.height / (max + panel.height) * height) :
                (int) (panel.width / (max + panel.width) * width);

        return Math.max(size, 10);
    }

    @Override
    public void onMoved() {
        var value = getMaxValue() <= 0 ? 0 : getValue();

        if (plane.isVertical) {
            panel.setScrollY(value);
        } else {
            panel.setScrollX(value);
        }
    }

    @Override
    public boolean canMouseScroll() {
        return super.canMouseScroll() || panel.isMouseOver();
    }

    @Override
    public boolean shouldDraw() {
        return getScrollBarSize() > 0;
    }

    @Override
    public boolean isEnabled() {
        return getScrollBarSize() > 0;
    }
}
