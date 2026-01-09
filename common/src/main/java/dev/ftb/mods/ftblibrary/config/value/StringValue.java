package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.AbstractEditableConfigValue;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

public class StringValue extends BaseValue<String> {
    @Nullable
    protected Pattern pattern;

    protected StringValue(ConfigGroup parent, String key, String defaultValue) {
        super(parent, key, defaultValue);
    }

    public StringValue pattern(Pattern pattern) {
        this.pattern = pattern;
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
    public void write(ConfigSerializer serializer) {
        serializer.putString(key, this);
    }

    @Override
    protected void addExtraHeaderInfo(List<String> header) {
        header.add("Default: \"" + defaultValue + "\"");
        if (pattern != null) {
            header.add("Regex: \"" + pattern + "\"");
        }
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getString(key, defaultValue));
    }

    @Override
    protected AbstractEditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addString(key, get(), this::set, defaultValue, pattern);
    }
}
