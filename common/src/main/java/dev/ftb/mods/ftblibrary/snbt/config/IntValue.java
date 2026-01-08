package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.util.Mth;

import java.util.Objects;

public class IntValue extends NumberValue<Integer> {
    IntValue(SNBTConfig config, String key, int defaultValue) {
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
    public void write(SNBTCompoundTag tag) {
        super.write(tag);
        tag.putInt(key, get());
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        set(tag.getIntOr(key, defaultValue));
    }

    @Override
    public void fillClientConfig(ConfigGroup group) {
        group.addInt(key, get(), this::set, defaultValue,
                        Objects.requireNonNullElse(minValue, Integer.MAX_VALUE), Objects.requireNonNullElse(maxValue, Integer.MAX_VALUE)
                )
                .fader(fader)
                .setCanEdit(enabled.getAsBoolean());
    }
}
