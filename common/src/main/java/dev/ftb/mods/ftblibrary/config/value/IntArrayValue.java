package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.AbstractEditableConfigValue;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableInt;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Arrays;
import java.util.List;

public class IntArrayValue extends BaseValue<int[]> {
    IntArrayValue(ConfigGroup parent, String key, int[] defaultValue) {
        super(parent, key, defaultValue);
        set(Arrays.copyOf(defaultValue, defaultValue.length));
    }

    @Override
    public void set(int[] v) {
        super.set(v);
    }

    @Override
    public void write(ConfigSerializer serializer) {
        serializer.putIntArray(key, this);
    }

    @Override
    protected void addExtraHeaderInfo(List<String> header) {
        header.add("Default: " + Arrays.toString(defaultValue));
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getIntArray(key, defaultValue));
    }

    @Override
    protected AbstractEditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addList(key, new IntArrayList(get()), new EditableInt(Integer.MIN_VALUE, Integer.MAX_VALUE), l -> set(l.stream().mapToInt(i -> i).toArray()), 0);
    }
}
