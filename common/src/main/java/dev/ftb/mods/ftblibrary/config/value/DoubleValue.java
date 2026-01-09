package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.AbstractEditableConfigValue;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import net.minecraft.util.Mth;

import java.util.Objects;

public class DoubleValue extends NumberValue<Double> {
    DoubleValue(ConfigGroup parent, String key, double defaultValue) {
        super(parent, key, defaultValue);
    }

    public NumberValue<Double> range(double max) {
        return range(0D, max);
    }

    @Override
    public void set(Double v) {
        super.set(Mth.clamp(v, minValue == null ? Double.NEGATIVE_INFINITY : minValue, maxValue == null ? Double.POSITIVE_INFINITY : maxValue));
    }

    @Override
    public void write(ConfigSerializer serializer) {
        serializer.putDouble(key, this);
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getDouble(key, defaultValue));
    }

    @Override
    protected AbstractEditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addDouble(key, get(), this::set, defaultValue,
                Objects.requireNonNullElse(minValue, Double.NEGATIVE_INFINITY),
                Objects.requireNonNullElse(maxValue, Double.POSITIVE_INFINITY)
        );
    }
}
