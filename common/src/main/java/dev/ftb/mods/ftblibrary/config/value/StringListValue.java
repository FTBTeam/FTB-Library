package dev.ftb.mods.ftblibrary.config.value;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;

import java.util.ArrayList;
import java.util.List;

public class StringListValue extends AbstractListValue<String> {
    StringListValue(Config parent, String key, List<String> defaultValue) {
        super(parent, key, defaultValue, Codec.STRING);

        super.set(new ArrayList<>(defaultValue));
    }

    @Override
    protected EditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addList(key, get(), new EditableString(null), "");
    }
}
