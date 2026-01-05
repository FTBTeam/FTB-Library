package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntArrayValue extends BaseValue<int[]> {

    IntArrayValue(SNBTConfig config, String key, int[] defaultValue) {
        super(config, key, defaultValue);
        set(Arrays.copyOf(defaultValue, defaultValue.length));
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        List<String> s = new ArrayList<>(comment);
        s.add("Default: " + Arrays.toString(defaultValue));
        tag.comment(key, String.join("\n", s));
        tag.putIntArray(key, get());
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        set(tag.getIntArray(key).orElse(defaultValue));
    }

    @Override
    public void createClientConfig(ConfigGroup group) {
    }
}
