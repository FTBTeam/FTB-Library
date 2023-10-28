package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.config.NameMap;

public enum PanelPositioning {
    BOTTOM_LEFT(0, 2),
    LEFT(0, 1),
    TOP_LEFT(0, 0),
    TOP_CENTER(1, 0),
    TOP_RIGHT(1, 0),
    RIGHT(1, 1),
    BOTTOM_RIGHT(1, 2),
    BOTTOM_CENTER(1, 2);

    public static final NameMap<PanelPositioning> NAME_MAP = NameMap.of(TOP_RIGHT, values()).baseNameKey("ftbquests.panel.position").create();

    public final int posX;
    public final int posY;

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
