package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.Tristate;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.network.chat.Component;

public class EditableBoolean extends EditableVariantConfig<Boolean> {
    public static final Component TRUE_TEXT = Component.literal("True");
    public static final Component FALSE_TEXT = Component.literal("False");

    @Override
    public Color4I getColor(Boolean value, Theme theme) {
        return Tristate.ofBoolean(value).getColor(theme);
    }

    @Override
    public Boolean getIteration(Boolean currentValue, boolean next) {
        return !currentValue;
    }

    @Override
    public Component getStringForGUI(Boolean value) {
        return value ? TRUE_TEXT : FALSE_TEXT;
    }

    @Override
    public Icon<?> getIcon(Boolean value) {
        return value ? Icons.ACCEPT : Icons.ACCEPT_GRAY;
    }
}
