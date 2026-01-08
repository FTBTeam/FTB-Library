package dev.ftb.mods.ftblibrary.client.gui.widget;


public class VerticalSpaceWidget extends Widget {
    public VerticalSpaceWidget(Panel p, int h) {
        super(p);
        setSize(1, h);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean shouldDraw() {
        return false;
    }
}
