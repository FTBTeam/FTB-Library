package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;

import java.util.List;

public class BooleanValue extends BaseValue<Boolean> {
    BooleanValue(Config parent, String key, boolean defaultValue) {
        super(parent, key, defaultValue);
    }

    public void toggle() {
        set(!get());
    }

    @Override
    public void write(ConfigSerializer serializer) {
        serializer.putBoolean(key, this);
    }

    @Override
    protected void addExtraHeaderInfo(List<String> header) {
        header.add("Default: " + defaultValue);
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getBoolean(key, defaultValue));
    }

    @Override
    protected EditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addBool(key, get(), this::set, defaultValue);
    }
}
