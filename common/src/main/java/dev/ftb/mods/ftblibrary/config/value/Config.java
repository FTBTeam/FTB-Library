package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import dev.ftb.mods.ftblibrary.util.NameMap;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A config group is basically a list of config values of any BaseValue type, including another ConfigGroup,
 * allowing for nested config groups.
 */
public class Config extends BaseValue<List<BaseValue<?>>> {
    private int displayOrder = 0;

    private Config(@Nullable Config parent, String key, List<BaseValue<?>> defaultValue) {
        super(parent, key, defaultValue);
    }

    /**
     * Create a new config object
     *
     * @param key the name for the config
     * @return the new config object
     */
    public static Config create(String key) {
        return new Config(null, key, new ArrayList<>());
    }

    @Override
    public void write(ConfigSerializer serializer) {
        serializer.putConfigSection(key, this, parent == null);
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getConfigSection(key, defaultValue, parent == null));
    }

    @Override
    protected EditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        List<BaseValue<?>> sorted = defaultValue.stream()
                .filter(v -> !v.excluded)
                .sorted(Comparator.comparingInt(o -> o.displayOrder))
                .toList();

        var actualGroup = parent == null ? group : group.getOrCreateSubgroup(key, displayOrder);
        sorted.forEach(value -> value.addToEditableConfigGroup(actualGroup));

        return null;
    }

    public <T extends BaseValue<?>> T add(T value) {
        defaultValue.add(value);
        return value;
    }

    public Config addGroup(String key) {
        return addGroup(key, 0);
    }

    public Config addGroup(String key, int displayOrder) {
        Config value = new Config(this, key, new ArrayList<>());
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
