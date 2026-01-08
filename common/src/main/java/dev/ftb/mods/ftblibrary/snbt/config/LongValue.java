package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;

public class LongValue extends NumberValue<Long> {
    LongValue(SNBTConfig config, String key, long defaultValue) {
        super(config, key, defaultValue);
    }

    public NumberValue<Long> range(long max) {
        return range(0L, max);
    }

    @Override
    public void set(Long v) {
        super.set(MathUtils.clamp(v, minValue == null ? Long.MIN_VALUE : minValue, maxValue == null ? Long.MAX_VALUE : maxValue));
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        super.write(tag);
        tag.putLong(key, get());
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        set(tag.getLongOr(key, defaultValue));
    }

    @Override
    public void fillClientConfig(ConfigGroup group) {
        group.addLong(key, get(), this::set, defaultValue, minValue == null ? Long.MIN_VALUE : minValue, maxValue == null ? Long.MAX_VALUE : maxValue)
                .fader(fader)
                .setCanEdit(enabled.getAsBoolean());
    }
}
