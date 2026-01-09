package dev.ftb.mods.ftblibrary.config.value;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.AbstractEditableConfigValue;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringListValue extends AbstractListValue<String> {
    StringListValue(ConfigGroup parent, String key, List<String> defaultValue) {
        super(parent, key, defaultValue, Codec.STRING);

        super.set(new ArrayList<>(defaultValue));
    }

    @Override
    protected AbstractEditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addList(key, get(), new EditableString(null), "");
    }
}
