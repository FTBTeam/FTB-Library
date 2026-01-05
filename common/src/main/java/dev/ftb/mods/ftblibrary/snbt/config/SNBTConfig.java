package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public final class SNBTConfig extends BaseValue<List<BaseValue<?>>> {
    private int displayOrder = 0;

    private SNBTConfig(SNBTConfig parent, String name, List<BaseValue<?>> defaultValue) {
        super(parent, name, defaultValue);
    }

    /**
     * Create a new SNBT config object
     * @param name the name for the config
     * @return the new config object
     */
    public static SNBTConfig create(String name) {
        return new SNBTConfig(null, name, new ArrayList<>());
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        if (parent == null) {
            tag.comment("", String.join("\n", comment));
            for (var value : defaultValue.stream().sorted().toList()) {
                value.write(tag);
            }
        } else {
            tag.comment(key, String.join("\n", comment));
            var newTag = new SNBTCompoundTag();
            for (var value : defaultValue.stream().sorted().toList()) {
                value.write(newTag);
            }
            tag.put(key, newTag);
        }
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        if (parent == null) {
            for (var value : defaultValue) {
                if (tag.contains(value.key)) {
                    value.read(tag);
                }
            }
        } else {
            if (tag.contains(key)) {
                var newTag = tag.getAsSnbtComponent(key);
                for (var value : defaultValue) {
                    if (newTag.contains(value.key)) {
                        value.read(newTag);
                    }
                }
            }
        }
    }

    @Override
    public void createClientConfig(ConfigGroup group) {
        List<BaseValue<?>> sorted = defaultValue.stream()
                .filter(v -> !v.excluded)
                .sorted(Comparator.comparingInt(o -> o.displayOrder))
                .toList();

        var g = parent == null ? group : group.getOrCreateSubgroup(key, displayOrder);
        sorted.forEach(value -> value.createClientConfig(g));
    }

    public void load(Path path) {
        try {
            SNBTCompoundTag tag = SNBT.tryRead(path);
            read(tag);
            save(path);
        } catch (IOException e) {
            FTBLibrary.LOGGER.error("can't read {}: {}, {}", path, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public void save(Path path) {
//		Util.ioPool().execute(() -> saveNow(path));
        saveNow(path);
    }

    public void saveNow(Path path) {
        if (parent != null) {
            parent.saveNow(path);
        } else {
            try {
                SNBT.tryWrite(path, Util.make(new SNBTCompoundTag(), this::write));
            } catch (IOException e) {
                FTBLibrary.LOGGER.error("can't write snbt to {} : {}/{}", path, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    public void load(Path path, Path defaultPath, Supplier<String[]> comment) {
        if (Files.exists(defaultPath)) {
            if (!Files.exists(path)) {
                try {
                    // it's possible for the destination directory to not exist yet
                    // in particular when creating a new world on Fabric
                    // https://github.com/FTBTeam/FTB-Mods-Issues/issues/867
                    Files.createDirectories(path.getParent());
                    Files.copy(defaultPath, path);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            var defaultConfigFile = SNBTConfig.create(key);
            defaultConfigFile.comment(comment.get());
            defaultConfigFile.save(defaultPath);
        }

        load(path);
    }

    public <T extends BaseValue<?>> T add(T value) {
        defaultValue.add(value);
        return value;
    }

    public SNBTConfig addGroup(String key) {
        return addGroup(key, 0);
    }

    public SNBTConfig addGroup(String key, int displayOrder) {
        SNBTConfig value = new SNBTConfig(this, key, new ArrayList<>());
        value.displayOrder = displayOrder;
        return add(value);
    }

    public BooleanValue addBoolean(String key, boolean defaultValue) {
        return add(new BooleanValue(this, key, defaultValue));
    }

    public IntValue addInt(String key, int defaultValue) {
        return add(new IntValue(this, key, defaultValue));
    }

    public IntValue addInt(String key, int defaultValue, int min, int max) {
        return addInt(key, defaultValue).range(min, max);
    }

    public LongValue addLong(String key, long defaultValue) {
        return add(new LongValue(this, key, defaultValue));
    }

    public LongValue addLong(String key, long defaultValue, long min, long max) {
        return addLong(key, defaultValue).range(min, max);
    }

    public DoubleValue addDouble(String key, double defaultValue) {
        return add(new DoubleValue(this, key, defaultValue));
    }

    public DoubleValue addDouble(String key, double def, double min, double max) {
        return addDouble(key, def).range(min, max);
    }

    public StringValue addString(String key, String defaultValue) {
        return add(new StringValue(this, key, defaultValue));
    }

    public StringListValue addStringList(String key, List<String> defaultValue) {
        return add(new StringListValue(this, key, defaultValue));
    }

    public <T> EnumValue<T> addEnum(String key, NameMap<T> nameMap) {
        return add(new EnumValue<>(this, key, nameMap));
    }

    public <T> EnumValue<T> addEnum(String key, NameMap<T> nameMap, T defaultValue) {
        return add(new EnumValue<>(this, key, nameMap, defaultValue));
    }

    public IntArrayValue addIntArray(String key, int[] defaultValue) {
        return add(new IntArrayValue(this, key, defaultValue));
    }
}
