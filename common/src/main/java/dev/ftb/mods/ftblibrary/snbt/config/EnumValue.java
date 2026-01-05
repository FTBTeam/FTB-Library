package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnumValue<T> extends BaseValue<T> {
    private final NameMap<T> nameMap;

    EnumValue(SNBTConfig config, String key, NameMap<T> nameMap) {
        this(config, key, nameMap, nameMap.defaultValue);
    }

    EnumValue(SNBTConfig config, String key, NameMap<T> nameMap, T defaultValue) {
        super(config, key, defaultValue);
        this.nameMap = nameMap;
    }

    @Override
    public void set(T value) {
        if (nameMap.values.contains(value)) {
            super.set(value);
        } else {
            super.set(defaultValue);
        }
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        List<String> s = new ArrayList<>(comment);
        s.add("Default: \"" + nameMap.getName(defaultValue) + "\"");
        s.add("Valid values: " + nameMap.keys.stream().map(StringTag::quoteAndEscape).collect(Collectors.joining(", ")));
        tag.comment(key, String.join("\n", s));
        tag.putString(key, nameMap.getName(get()));
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        var value = tag.getString(key);
        if (value.isEmpty()) {
            set(defaultValue);
            return;
        }

        set(nameMap.get(value.get()));
    }

    @Override
    public void createClientConfig(ConfigGroup group) {
        group.addEnum(key, get(), this::set, nameMap)
                .setCanEdit(enabled.getAsBoolean())
        ;
    }
}
