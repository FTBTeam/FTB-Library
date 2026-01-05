package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.SNBTUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public abstract class BaseValue<T> implements Comparable<BaseValue<T>> {
    public final SNBTConfig parent;
    public final String key;
    protected final T defaultValue;
    protected boolean excluded;
    protected BooleanSupplier enabled = SNBTUtils.ALWAYS_TRUE;
    protected int displayOrder = 0;
    protected List<String> comment = new ArrayList<>(0);
    private T value;

    protected BaseValue(@Nullable SNBTConfig config, String key, T defaultValue) {
        parent = config;
        this.key = key;
        this.defaultValue = defaultValue;
        value = this.defaultValue;
    }

    @Override
    public String toString() {
        if (parent == null) {
            return key;
        }

        return parent + "/" + key;
    }

    public String getKey() {
        return key;
    }

    public T get() {
        return value;
    }

    public void set(T v) {
        value = v;
    }

    @SuppressWarnings("unchecked")
    public <E extends BaseValue<T>> E comment(String... comment) {
        this.comment.addAll(Arrays.asList(comment));
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public <E extends BaseValue<T>> E excluded() {
        excluded = true;
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public <E extends BaseValue<T>> E enabled(BooleanSupplier enabled) {
        this.enabled = enabled;
        return (E) this;
    }

    public abstract void write(SNBTCompoundTag tag);

    public abstract void read(SNBTCompoundTag tag);

    private int getOrder() {
        return this instanceof SNBTConfig ? 1 : 0;
    }

    public BaseValue<T> withDisplayOrder(int order) {
        this.displayOrder = order;
        return this;
    }

    @Override
    public int compareTo(BaseValue<T> other) {
        var i = Integer.compare(getOrder(), other.getOrder());
        return i == 0 ? key.compareToIgnoreCase(other.key) : i;
    }

    public void createClientConfig(ConfigGroup group) {
    }
}
