package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.AbstractEditableConfigValue;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import dev.ftb.mods.ftblibrary.math.MathUtils;

import java.util.Objects;

public class LongValue extends NumberValue<Long> {
    LongValue(ConfigGroup parent, String key, long defaultValue) {
        super(parent, key, defaultValue);
    }

    public NumberValue<Long> range(long max) {
        return range(0L, max);
    }

    @Override
    public void set(Long v) {
        super.set(MathUtils.clamp(v, minValue == null ? Long.MIN_VALUE : minValue, maxValue == null ? Long.MAX_VALUE : maxValue));
    }

    @Override
    public void write(ConfigSerializer serializer) {
        serializer.putLong(key, this);
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getLong(key, defaultValue));
    }

    @Override
    protected AbstractEditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addLong(key, get(), this::set, defaultValue,
                Objects.requireNonNullElse(minValue, Long.MIN_VALUE),
                Objects.requireNonNullElse(maxValue, Long.MAX_VALUE));
    }
}
