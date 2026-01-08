package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public abstract class AbstractEditableNumber<T extends Number> extends AbstractEditableStringifiedConfig<T> {
    public static final Color4I COLOR = Color4I.rgb(0xAA5AE8);

    public final T min;
    public final T max;
    public boolean fader;
    protected T scrollIncrement;

    public AbstractEditableNumber(T mn, T mx) {
        min = mn;
        max = mx;
    }

    @Override
    public Color4I getColor(@Nullable T value, Theme theme) {
        return COLOR;
    }

    public AbstractEditableNumber<T> fader(boolean v) {
        fader = v;
        return this;
    }

    @Override
    public boolean canScroll() {
        return true;
    }

    @Override
    public Component getStringForGUI(@Nullable T v) {
        return v == null ? NULL_TEXT : Component.literal(formatValue(v));
    }

    protected String formatValue(T v) {
        return StringUtils.formatDouble(v.doubleValue(), true);
    }

    public AbstractEditableNumber<T> withScrollIncrement(T increment) {
        scrollIncrement = increment;
        return this;
    }
}
