package dev.ftb.mods.ftblibrary.client.gui;


import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;

public class MismatchingParentPanelException extends IllegalArgumentException {
    public final Panel panel;
    public final Widget widget;

    public MismatchingParentPanelException(Panel p, Widget w) {
        super("Widget's parent panel [" + w.getParent() + "] doesn't match the panel it was added to! [" + p + "]");
        panel = p;
        widget = w;
    }
}
