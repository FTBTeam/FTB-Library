package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;

import java.util.ArrayList;
import java.util.List;

public abstract class NumberValue<T extends Number> extends BaseValue<T> {
    protected T minValue = null;
    protected T maxValue = null;
    protected boolean fader;

    NumberValue(SNBTConfig c, String n, T def) {
        super(c, n, def);
    }

    @SuppressWarnings("unchecked")
    public <E extends BaseValue<T>> E range(T min, T max) {
        minValue = min;
        maxValue = max;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public <E extends BaseValue<T>> E fader() {
        fader = true;
        return (E) this;
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        List<String> c = new ArrayList<>(comment);
        c.add("Default: " + defaultValue);
        c.add("Range: " + (minValue == null ? "-∞" : minValue) + " ~ " + (maxValue == null ? "+∞" : maxValue));
        tag.comment(key, String.join("\n", c));
        // tag.putLong(key, get());
    }
}
