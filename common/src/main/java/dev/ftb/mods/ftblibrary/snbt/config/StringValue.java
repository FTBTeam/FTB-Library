package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringValue extends BaseValue<String> {
    protected Pattern pattern;

    protected StringValue(SNBTConfig config, String key, String defaultValue) {
        super(config, key, defaultValue);
    }

    public StringValue pattern(Pattern p) {
        pattern = p;
        return this;
    }

    @Override
    public void set(String value) {
        super.set(value);

        if (pattern != null && !pattern.matcher(get()).find()) {
            super.set(defaultValue);
        }
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        List<String> s = new ArrayList<>(comment);
        s.add("Default: \"" + defaultValue + "\"");
        tag.comment(key, String.join("\n", s));
        tag.putString(key, get());
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        set(tag.getStringOr(key, defaultValue));
    }

    @Override
    public void fillClientConfig(ConfigGroup group) {
        group.addString(key, get(), this::set, defaultValue, pattern)
                .setCanEdit(enabled.getAsBoolean());
    }
}
