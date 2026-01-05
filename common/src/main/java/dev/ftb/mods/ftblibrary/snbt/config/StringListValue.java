package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringListValue extends BaseValue<List<String>> {
    StringListValue(SNBTConfig config, String key, List<String> defaultValue) {
        super(config, key, defaultValue);
        super.set(new ArrayList<>(defaultValue));
    }

    @Override
    public void set(List<String> value) {
        get().clear();
        get().addAll(value);
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        List<String> s = new ArrayList<>(comment);
        s.add("Default: [" + defaultValue.stream().map(StringTag::quoteAndEscape).collect(Collectors.joining(", ")) + "]");
        tag.comment(key, String.join("\n", s));

        var stag = new ListTag();

        for (var s1 : get()) {
            stag.add(StringTag.valueOf(s1));
        }

        tag.put(key, stag);
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        var stag = tag.get(key);
        if (stag instanceof ListTag l && (l.isEmpty() || l.getFirst() instanceof StringTag)) {
            get().clear();

            for (var i = 0; i < l.size(); i++) {
                get().add(l.getStringOr(i, ""));
            }
        }
    }

    @Override
    public void createClientConfig(ConfigGroup group) {
        group.addList(key, get(), new StringConfig(null), "")
                .setCanEdit(enabled.getAsBoolean());
    }
}
