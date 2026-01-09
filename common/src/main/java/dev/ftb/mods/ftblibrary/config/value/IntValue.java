package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import net.minecraft.util.Mth;

import java.util.Objects;

public class IntValue extends NumberValue<Integer> {
    IntValue(Config config, String key, int defaultValue) {
        super(config, key, defaultValue);
    }

    public NumberValue<Integer> range(int max) {
        return range(0, max);
    }

    @Override
    public void set(Integer value) {
        super.set(Mth.clamp(value, Objects.requireNonNullElse(minValue, Integer.MAX_VALUE), Objects.requireNonNullElse(maxValue, Integer.MAX_VALUE)));
    }

    @Override
    public void write(ConfigSerializer serializer) {
        serializer.putInt(key, this);
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getInt(key, defaultValue));
    }

    @Override
    protected EditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addInt(key, get(), this::set, defaultValue,
                Objects.requireNonNullElse(minValue, Integer.MAX_VALUE),
                Objects.requireNonNullElse(maxValue, Integer.MAX_VALUE)
        );
    }
}
