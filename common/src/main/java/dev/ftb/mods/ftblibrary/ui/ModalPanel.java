package dev.ftb.mods.ftblibrary.ui;

public abstract class ModalPanel extends Panel {
    int extraZlevel = 1;

    public ModalPanel(Panel panel) {
        super(panel);
    }

    @Override
    public boolean checkMouseOver(int mouseX, int mouseY) {
        var ax = getX();
        var ay = getY();
        return mouseX >= ax && mouseY >= ay && mouseX < ax + width && mouseY < ay + height;
    }

    public int getExtraZlevel() {
        return extraZlevel;
    }

    public void setExtraZlevel(int extraZlevel) {
        this.extraZlevel = extraZlevel;
    }
}
