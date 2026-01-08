package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.client.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.AbstractEditableConfigValue;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.SNBTUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Top-level class for all config values
 *
 * @param <T> the value's type
 */
public abstract class BaseValue<T> implements Comparable<BaseValue<T>> {
    public final SNBTConfig parent;
    public final String key;
    protected final T defaultValue;
    protected boolean excluded;
    protected BooleanSupplier enabled = SNBTUtils.ALWAYS_TRUE;
    protected int displayOrder = 0;
    protected List<String> comment = new ArrayList<>(0);
    private T value;

    protected BaseValue(@Nullable SNBTConfig parent, String key, T defaultValue) {
        this.parent = parent;
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

    public <E extends BaseValue<T>> E comment(String... comment) {
        this.comment.addAll(Arrays.asList(comment));
        return self();
    }

    public <E extends BaseValue<T>> E excluded() {
        excluded = true;
        return self();
    }

    public <E extends BaseValue<T>> E enabled(BooleanSupplier enabled) {
        this.enabled = enabled;
        return self();
    }

    private <E extends BaseValue<T>> E self() {
        //noinspection unchecked
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

    /**
     * Called when a client-side ConfigGroup is being created via
     * {@link dev.ftb.mods.ftblibrary.config.manager.ConfigManagerClient#editConfig(String)}. Implementations should
     * add a suitable {@link AbstractEditableConfigValue} field to the given config group for this config value (many
     * convenience methods exist in {@code ConfigGroup} for this).
     * <p>
     * This method should, of course, only be called on the client.
     *
     * @param group the config group being filled out
     */
    public void fillClientConfig(ConfigGroup group) {
    }
}
