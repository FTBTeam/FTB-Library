package dev.ftb.mods.ftblibrary.client.gui;

import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;

public interface IScreenWrapper extends IOpenableScreen {
    BaseScreen getGui();

    @Override
    default void openGui() {
        getGui().openGui();
    }

    @Override
    default void closeGui(boolean openPrevScreen) {
        getGui().closeGui(openPrevScreen);
    }
}