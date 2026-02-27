package dev.ftb.mods.ftblibrary.client.gui;

import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;


public interface IOpenableScreen extends Runnable {
    void openGui();

    default void openGuiLater() {
        ClientUtils.runLater(this);
    }

    default void closeGui() {
        closeGui(true);
    }

    default void closeGui(boolean openPrevScreen) {
    }

    default void closeContextMenu() {
        if (this instanceof Widget w) {
            w.getGui().closeContextMenu();
        }
    }

    @Override
    default void run() {
        if (ClientUtils.getCurrentGuiAs(IOpenableScreen.class) != this) {
            openGui();
        }
    }

    default Runnable openAfter(Runnable runnable) {
        return () -> {
            runnable.run();
            IOpenableScreen.this.run();
        };
    }
}
