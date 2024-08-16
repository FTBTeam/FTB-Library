package dev.ftb.mods.ftblibrary.ui;

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