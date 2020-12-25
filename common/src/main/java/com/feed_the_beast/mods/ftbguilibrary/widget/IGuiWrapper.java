package com.feed_the_beast.mods.ftbguilibrary.widget;

public interface IGuiWrapper extends IOpenableGui {
    GuiBase getGui();

    @Override
    default void openGui() {
        getGui().openGui();
    }

    @Override
    default void closeGui(boolean openPrevScreen) {
        getGui().closeGui(openPrevScreen);
    }
}