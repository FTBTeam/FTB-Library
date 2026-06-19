package dev.ftb.mods.ftblibrary.ui2.components;

public abstract class Component {
    private String name;

    public Component(String name) {
        this.name = name;
    }

    abstract void render(int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks);
}
