package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.gui.EditStringConfigOverlay;
import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractEditableStringifiedConfig<T> extends AbstractEditableConfigValue<T> {
    public abstract boolean parse(@Nullable Consumer<T> callback, String string);

    public String getStringFromValue(@Nullable T v) {
        return String.valueOf(v);
    }

    @Override
    public Component getStringForGUI(@Nullable T v) {
        return Component.literal(getStringFromValue(v));
    }

    @Override
    public void onClicked(Widget clicked, MouseButton button, ConfigCallback callback) {
        BaseScreen gui = clicked.getGui();

        EditStringConfigOverlay.PosProvider.Offset offset = clicked instanceof EditStringConfigOverlay.PosProvider p ?
                p.getOverlayOffset() :
                EditStringConfigOverlay.PosProvider.Offset.NONE;
        int xPos = clicked.getX() - gui.getX() + offset.x();
        int yPos = clicked.getY() - gui.getY() + offset.y();

        EditStringConfigOverlay<T> panel = new EditStringConfigOverlay<>(gui, this, callback);
        panel.setPos(xPos, yPos);
        gui.pushModalPanel(panel);
    }

    public boolean canScroll() {
        return false;
    }

    public Optional<T> scrollValue(T currentValue, boolean forward) {
        return Optional.empty();
    }

    protected boolean okValue(@Nullable Consumer<T> callback, T v) {
        if (callback != null) callback.accept(v);
        return true;
    }
}
