package dev.ftb.mods.ftblibrary.ui2.components;

public abstract class Component {
    private String name;

    public Component(String name) {
        this.name = name;
    }

    /**
     * We need a way of having components that can negotiate space with each other.
     */
    public abstract Size sizeThatFits(Size proposal);

    public abstract void placeSubviews(int x, int y, int width, int height);

    abstract void render(int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks);

    public record Size(int width, int height) {
    }
}
