package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.AbstractEditableConfigValue;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import dev.ftb.mods.ftblibrary.util.NameMap;
import net.minecraft.nbt.StringTag;

import java.util.List;
import java.util.stream.Collectors;

public class EnumValue<T> extends BaseValue<T> {
    private final NameMap<T> nameMap;

    EnumValue(ConfigGroup parent, String key, NameMap<T> nameMap) {
        this(parent, key, nameMap, nameMap.defaultValue);
    }

    EnumValue(ConfigGroup parent, String key, NameMap<T> nameMap, T defaultValue) {
        super(parent, key, defaultValue);
        this.nameMap = nameMap;
    }

    public NameMap<T> getNameMap() {
        return nameMap;
    }

    @Override
    public void set(T value) {
        super.set(nameMap.values.contains(value) ? value : defaultValue);
    }

    @Override
    public void write(ConfigSerializer serializer) {
        serializer.putEnum(key, this, nameMap);
    }

    @Override
    protected void addExtraHeaderInfo(List<String> header) {
        header.add("Default: \"" + nameMap.getName(defaultValue) + "\"");
        header.add("Valid values: " + nameMap.keys.stream().map(StringTag::quoteAndEscape).collect(Collectors.joining(", ")));
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getEnum(key, defaultValue, nameMap));
    }

    @Override
    protected AbstractEditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addEnum(key, get(), this::set, nameMap);
    }
}
