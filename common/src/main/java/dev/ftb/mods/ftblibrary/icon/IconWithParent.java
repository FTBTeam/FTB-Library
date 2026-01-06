package dev.ftb.mods.ftblibrary.icon;


public abstract class IconWithParent<T extends Icon<T>> extends Icon<T> {
    private final Icon<?> parent;

    protected IconWithParent(Icon<?> parent) {
        this.parent = parent;
    }

    public Icon<?> getParent() {
        return parent;
    }
}
