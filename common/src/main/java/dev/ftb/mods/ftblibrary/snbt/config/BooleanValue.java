package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;

import java.util.ArrayList;
import java.util.List;

public class BooleanValue extends BaseValue<Boolean> {
    BooleanValue(SNBTConfig config, String key, boolean defaultValue) {
        super(config, key, defaultValue);
    }

    public void toggle() {
        set(!get());
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        List<String> s = new ArrayList<>(comment);
        s.add("Default: " + defaultValue);
        tag.comment(key, String.join("\n", s));
        tag.putBoolean(key, get());
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        set(tag.getBooleanOr(key, defaultValue));
    }

    @Override
    public void fillClientConfig(ConfigGroup group) {
        group.addBool(key, get(), this::set, defaultValue).setCanEdit(enabled.getAsBoolean());
    }
}
