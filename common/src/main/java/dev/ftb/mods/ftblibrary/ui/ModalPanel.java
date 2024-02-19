package dev.ftb.mods.ftblibrary.ui;

public abstract class ModalPanel extends Panel {
    public ModalPanel(Panel panel) {
        super(panel);
    }

    @Override
    public boolean checkMouseOver(int mouseX, int mouseY) {
        var ax = getX();
        var ay = getY();
        return mouseX >= ax && mouseY >= ay && mouseX < ax + width && mouseY < ay + height;
    }
}
