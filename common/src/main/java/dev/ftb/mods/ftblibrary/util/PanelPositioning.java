package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.config.NameMap;

public enum PanelPositioning {
    // 0: align left/top, 1: align center, 2: align right/bottom
    TOP_LEFT(0, 0),
    TOP(1, 0),
    TOP_RIGHT(2, 0),
    RIGHT(2, 1),
    BOTTOM_RIGHT(2, 2),
    BOTTOM(1, 2),
    BOTTOM_LEFT(0, 2),
    LEFT(0, 1);

    public static final NameMap<PanelPositioning> NAME_MAP = NameMap.of(TOP_RIGHT, values()).baseNameKey("ftblibrary.panel.position").create();

    private final int posX;
    private final int posY;

    PanelPositioning(int x, int y) {
        posX = x;
        posY = y;
    }

    public PanelPos getPanelPos(int screenW, int screenH, int panelW, int panelH, int insetX, int insetY) {
        int px = switch (posX) {
            case 0 -> insetX;
            case 1 -> (screenW - panelW) / 2;
            default -> screenW - panelW - insetX;
        };
        int py = switch (posY) {
            case 0 -> insetY;
            case 1 -> (screenH - panelH) / 2;
            default -> screenH - panelH - insetY;
        };
        return new PanelPos(px, py);
    }

    public PanelPos getPanelPos(int screenW, int screenH, int panelW, int panelH, float insetX, float insetY) {
        return getPanelPos(screenW, screenH, panelW, panelH, (int)(screenW * insetX / 2), (int)(screenH * insetY / 2));
    }

    public record PanelPos(int x, int y) {
    }
}
