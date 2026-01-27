package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public abstract class EditableNumber<T extends Number> extends EditableStringifiedConfig<T> {
    public static final Color4I COLOR = Color4I.rgb(0xAA5AE8);

    public final T min;
    public final T max;
    protected T scrollIncrement;

    public EditableNumber(T min, T max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Color4I getColor(T value, Theme theme) {
        return theme.hasDarkBackground() ? COLOR : Color4I.rgb(0xFF9026A0);
    }

    @Override
    public boolean canScroll() {
        return true;
    }

    @Override
    public Component getStringForGUI(T value) {
        return Component.literal(formatValue(value));
    }

    protected String formatValue(T v) {
        return StringUtils.formatDouble(v.doubleValue(), true);
    }

    public EditableNumber<T> withScrollIncrement(T increment) {
        scrollIncrement = increment;
        return this;
    }
}
