package dev.ftb.mods.ftblibrary.math;

import net.minecraft.util.StringRepresentable;


public enum ScreenPosition implements StringRepresentable {
    CENTER("center", 0, 0),
    TOP("top", 0, -1),
    BOTTOM("bottom", 0, 1),
    LEFT("left", -1, 0),
    RIGHT("right", 1, 0),
    TOP_LEFT("top_left", -1, -1),
    TOP_RIGHT("top_right", 1, -1),
    BOTTOM_LEFT("bottom_left", -1, 1),
    BOTTOM_RIGHT("bottom_right", 1, 1);

    private final String name;
    private final int offsetX, offsetY;

    ScreenPosition(String n, int ox, int oy) {
        name = n;
        offsetX = ox;
        offsetY = oy;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getX(int screenWidth, int width, int offset) {
        return switch (offsetX) {
            case -1 -> offset;
            case 1 -> (screenWidth - width) / 2;
            default -> screenWidth - width - offset;
        };
    }

    public int getY(int screenHeight, int height, int offset) {
        return switch (offsetY) {
            case -1 -> offset;
            case 1 -> (screenHeight - height) / 2;
            default -> screenHeight - height - offset;
        };
    }
}
