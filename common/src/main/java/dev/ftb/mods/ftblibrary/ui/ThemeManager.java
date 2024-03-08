package dev.ftb.mods.ftblibrary.ui;

/**
 * This is just a stub for now. Eventually there will be a way for players to select a UI theme!
 */
public enum ThemeManager {
    INSTANCE;

    public Theme getActiveTheme() {
        return NordTheme.THEME;
//        return Theme.DEFAULT;
    }
}
