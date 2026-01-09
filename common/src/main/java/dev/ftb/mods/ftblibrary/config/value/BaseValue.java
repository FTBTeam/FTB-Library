package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.AbstractEditableConfigValue;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
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
    public final ConfigGroup parent;
    public final String key;
    protected final T defaultValue;
    protected boolean excluded;
    protected BooleanSupplier enabled = SNBTUtils.ALWAYS_TRUE;
    protected int displayOrder = 0;
    protected List<String> comment = new ArrayList<>(0);
    private T value;

    protected BaseValue(@Nullable ConfigGroup parent, String key, T defaultValue) {
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

    /**
     * Add a (possibly multiline) comment for this config value. This comment will appear with the value in the saved
     * config file, along with possible information about the value (e.g. acceptable value, range, etc.)
     *
     * @param comment the comment text, one string per line
     * @return the value itself, for fluency
     */
    public <E extends BaseValue<T>> E comment(String... comment) {
        this.comment.addAll(Arrays.asList(comment));
        return self();
    }

    /**
     * Mark this config value as being excluded from GUI config editing, even the value's type is normally editable.
     * This can be used for values that may be managed automatically, where direct player editing is undesirable.
     *
     * @return the value itself, for fluency
     */
    public <E extends BaseValue<T>> E excludedFromGui() {
        excluded = true;
        return self();
    }

    /**
     * Specify a predicate to control whether this config value is enabled for editing in the config GUI. Entries which
     * are disabled appear greyed out in the GUI and can't be changed interactively, but their values are still
     * readable and writable via API.
     *
     * @param enabled the predicate
     * @return the value itself, for fluency
     */
    public <E extends BaseValue<T>> E enabledForEdit(BooleanSupplier enabled) {
        this.enabled = enabled;
        return self();
    }

    /**
     * Specify a display order for this entry to control its ordering within its config section in the GUI. By default,
     * all entries have an ordering of 0. Entries with the same order are sorted alphabetically by their display text.
     *
     * @param order the ordering
     * @return the value itself, for fluency
     */
    public <E extends BaseValue<T>> E withDisplayOrder(int order) {
        this.displayOrder = order;
        return self();
    }

    protected <E extends BaseValue<T>> E self() {
        //noinspection unchecked
        return (E) this;
    }

    public abstract void write(ConfigSerializer serializer);

    public abstract void read(ConfigSerializer serializer);

    private int getOrder() {
        return this instanceof ConfigGroup ? 1 : 0;
    }

    @Override
    public int compareTo(BaseValue<T> other) {
        var i = Integer.compare(getOrder(), other.getOrder());
        return i == 0 ? key.compareToIgnoreCase(other.key) : i;
    }

    public final void addToEditableConfigGroup(EditableConfigGroup group) {
        var editable = fillClientConfig(group);
        if (editable != null) {
            editable.setCanEdit(enabled.getAsBoolean());
        }
    }

    /**
     * Called when a client-side EditableConfigGroup is being created via
     * {@link dev.ftb.mods.ftblibrary.config.manager.ConfigManagerClient#editConfig(String)}. Implementations should
     * add a suitable {@link AbstractEditableConfigValue} field to the given config group for this config value (many
     * convenience methods exist in {@code ConfigGroup} for this).
     * <p>
     * This method should, of course, only be called on the client.
     *
     * @param group the config group being filled out
     * @return the editable field that was added, or null if this type isn't editable in the config GUI
     */
    @Nullable
    protected AbstractEditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return null;
    }

    /**
     * Override this method to add extra information about this config value type. This information will be included
     * in comments in the saved config file.
     *
     * @param header the header list to add lines to
     */
    protected void addExtraHeaderInfo(List<String> header) {
    }

    @Nullable
    public final String getCommentString() {
        List<String> c = new ArrayList<>(comment);
        addExtraHeaderInfo(c);
        return c.isEmpty() ? null : String.join("\n", c);
    }
}
