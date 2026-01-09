package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.Tristate;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class EditableBoolean extends EditableVariantConfig<Boolean> {
    public static final Component TRUE_TEXT = Component.literal("True");
    public static final Component FALSE_TEXT = Component.literal("False");

    @Override
    public Color4I getColor(@Nullable Boolean value, Theme theme) {
        return value == null || !value ? Tristate.FALSE.color : Tristate.TRUE.color;
    }

    @Override
    public Boolean getIteration(Boolean currentValue, boolean next) {
        return !currentValue;
    }

    @Override
    public Component getStringForGUI(@Nullable Boolean v) {
        return v == null ? NULL_TEXT : v ? TRUE_TEXT : FALSE_TEXT;
    }

    @Override
    public Icon<?> getIcon(@Nullable Boolean value) {
        return value == null || !value ? Icons.ACCEPT_GRAY : Icons.ACCEPT;
    }
}
