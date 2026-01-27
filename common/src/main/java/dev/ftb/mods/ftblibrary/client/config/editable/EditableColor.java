package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.gui.widget.ColorSelectorPanel;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class EditableColor extends EditableConfigValue<Color4I> {
    private boolean allowAlphaEdit = false;

    public EditableColor() {
        defaultValue = Icon.empty();
        value = Icon.empty();
    }

    public EditableColor withAlphaEditing() {
        allowAlphaEdit = true;
        return this;
    }

    public boolean isAllowAlphaEdit() {
        return allowAlphaEdit;
    }

    @Override
    public void onClicked(Widget clicked, MouseButton button, ConfigCallback callback) {
        ColorSelectorPanel.popupAtMouse(clicked.getGui(), this, callback);
    }

    @Override
    public Component getStringForGUI(Color4I value) {
        return super.getStringForGUI(value).copy().append(Component.literal(" ■").withColor(allowAlphaEdit ? this.value.rgba() : this.value.rgb()));
    }
}
