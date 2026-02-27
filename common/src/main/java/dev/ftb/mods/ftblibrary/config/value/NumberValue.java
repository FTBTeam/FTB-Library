package dev.ftb.mods.ftblibrary.config.value;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class NumberValue<T extends Number> extends BaseValue<T> {
    @Nullable
    protected T minValue = null;
    @Nullable
    protected T maxValue = null;

    NumberValue(Config parent, String key, T defaultValue) {
        super(parent, key, defaultValue);
    }

    public <E extends BaseValue<T>> E range(T min, T max) {
        minValue = min;
        maxValue = max;
        return self();
    }

    @Override
    protected void addExtraHeaderInfo(List<String> header) {
        header.add(String.format("Default: %s | Range: %s ~ %s",
                defaultValue,
                Objects.requireNonNullElse(minValue, "-∞"),
                Objects.requireNonNullElse(maxValue, "+∞"))
        );
    }
}
