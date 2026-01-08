package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.util.Mth;

public class DoubleValue extends NumberValue<Double> {
    DoubleValue(SNBTConfig config, String key, double defaultValue) {
        super(config, key, defaultValue);
    }

    public NumberValue<Double> range(double max) {
        return range(0D, max);
    }

    @Override
    public void set(Double v) {
        super.set(Mth.clamp(v, minValue == null ? Double.NEGATIVE_INFINITY : minValue, maxValue == null ? Double.POSITIVE_INFINITY : maxValue));
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        super.write(tag);
        tag.putDouble(key, get());
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        set(tag.getDoubleOr(key, defaultValue));
    }

    @Override
    public void fillClientConfig(ConfigGroup group) {
        group.addDouble(key, get(), this::set, defaultValue, minValue == null ? Double.NEGATIVE_INFINITY : minValue, maxValue == null ? Double.POSITIVE_INFINITY : maxValue)
                .fader(fader)
                .setCanEdit(enabled.getAsBoolean());
    }
}
