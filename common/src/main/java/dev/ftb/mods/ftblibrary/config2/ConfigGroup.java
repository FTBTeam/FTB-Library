package dev.ftb.mods.ftblibrary.config2;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConfigGroup extends AbstractValueCreator {
    private static Logger LOGGER = LoggerFactory.getLogger(ConfigGroup.class);

    private final FTBConfig parent;
    public final String key;

    private final List<ConfigValue<?>> values = new ArrayList<>();
    private final List<ConfigGroup> groups = new ArrayList<>();

    public ConfigGroup(FTBConfig ftbConfig, String key) {
        this.parent = ftbConfig;
        this.key = key;
    }

    @Override
    <T> ConfigValue<T> addValue(String key, T defaultValue, Codec<T> codec) {
        var config = new ConfigValue<>(this.parent, key, defaultValue, codec);
        this.values.add(config);
        return config;
    }

    public ConfigGroup group(String key) {
        var group = new ConfigGroup(this.parent, key);
        this.groups.add(group);
        return group;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        for (ConfigValue<?> value : this.values) {
            tag.put(value.key(), value.write());
        }

        for (ConfigGroup group : this.groups) {
            tag.put(group.key, group.toTag());
        }

        return tag;
    }

    public void fromTag(CompoundTag tag) {
        for (ConfigValue<?> value : this.values) {
            if (tag.contains(value.key())) {
                var nbt = tag.get(value.key());
                value.readFromTag(nbt);
            } else {
                LOGGER.error("Missing config value: {}", value.key());
            }
        }

        for (ConfigGroup group : this.groups) {
            if (tag.contains(group.key)) {
                group.fromTag(tag.getCompound(group.key).orElseThrow());
            } else {
                LOGGER.error("Missing config group: {}", group.key);
            }
        }
    }
}
