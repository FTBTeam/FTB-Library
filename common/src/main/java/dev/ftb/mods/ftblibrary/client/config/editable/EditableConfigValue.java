package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.config.value.BaseValue;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base class for all editable configs. This is the "glue" between config objects as loaded from disk (see
 * {@link BaseValue} and the GUI config editor.
 *
 * @param <T> the type of object being edited
 */
public abstract class EditableConfigValue<T> implements Comparable<EditableConfigValue<T>> {
    public static final Component NULL_TEXT = Component.literal("null");

    public String id = "";
    @Nullable protected T value;
    @Nullable protected T defaultValue;
    private EditableConfigGroup group;
    private Consumer<T> setter;
    private int order = 0;
    private String nameKey = "";
    private Icon<?> icon = Icons.SETTINGS;
    private boolean canEdit = true;

    protected static Component info(String key) {
        return Component.literal(key + ":").withStyle(ChatFormatting.AQUA);
    }

    public static Component info(String key, Object value) {
        var title = value instanceof Component c ? c : Component.literal(String.valueOf(value));
        return Component.empty().append((Component.literal(key + ": ").withStyle(ChatFormatting.AQUA))).append(title);
    }

    /**
     * Initialise this config value; called when it's added to a config group with
     * {@link EditableConfigGroup#add(String, EditableConfigValue, Object, Consumer, Object)}
     *
     * @param group the group being added to
     * @param id a unique id for this value
     * @param value the initial value from the underlying config
     * @param setter a consumer to be called to apply changes back to the underlying config
     * @param defaultValue the default value
     * @return the initialised config value
     */
    public EditableConfigValue<T> init(EditableConfigGroup group, String id, @Nullable T value, Consumer<T> setter, @Nullable T defaultValue) {
        this.group = group;
        this.id = id;
        this.value = value == null ? null : copy(value);
        this.setter = setter;
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * {@return the config group to which this editable belongs}
     */
    public EditableConfigGroup getGroup() {
        return group;
    }

    /**
     * {@return the current value for this editable}
     */
    @Nullable
    public T getValue() {
        return value;
    }

    /**
     * Unconditionally set a new value. See also {@link #updateValue(Object)}
     * @param newValue the new value
     */
    public void setValue(@Nullable T newValue) {
        this.value = newValue;
    }

    /**
     * Update the current value of this editable. This should be called from GUI code when a change shoud be applied.
     *
     * @param newValue the new value
     * @return true if the value was actually changed, false if the newValue is the same as value
     */
    public final boolean updateValue(@Nullable T newValue) {
        if (!isEqual(value, newValue)) {
            value = newValue;
            return true;
        }

        return false;
    }

    /**
     * {@return the default value for this editable}
     */
    @Nullable
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value for this editable. This is required if the value is some kind of container object, e.g.
     * a list or a map.
     * @param defaultValue the new default value
     */
    public void setDefaultValue(@Nullable T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Compare two values for equality.
     *
     * @param v1 the first value
     * @param v2 the second value
     * @return true if the values are equal
     */
    public boolean isEqual(@Nullable T v1, @Nullable T v2) {
        return Objects.equals(v1, v2);
    }

    /**
     * Make a deep copy of this editable. Override this for mutable objects (e.g. lists, itemstacks) to correctly
     * return a new object. For immutable objects, returning the passed object is fine.
     *
     * @param value the value to copy
     * @return the new value
     */
    public T copy(T value) {
        return value;
    }

    /**
     * Get the color to represent the current value of this editable, for display purposes in the editor GUI.
     *
     * @param theme the GUI theme currently being used
     * @return the display color
     *
     * @implNote themes are currently ignored but the theme parameter is in the API now, which is a start!
     */
    public final Color4I getColor(Theme theme) {
        return getColor(value, theme);
    }

    /**
     * Get the color to represent some specific value of this editable, for display purposes in the editor GUI.
     *
     * @param value the object
     * @param theme the GUI theme currently being used
     * @return the display color
     *
     * @implNote themes are currently ignored but the theme parameter is in the API now, which is a start!
     */
    public Color4I getColor(@Nullable T value, Theme theme) {
        return Color4I.GRAY;
    }

    /**
     * Add some descriptive text for this editable, for tooltip purposes in the editor GUI.
     *
     * @param list the tooltip list to append to
     */
    public void addInfo(TooltipList list) {
        list.add(info("Default", getStringForGUI(defaultValue)));
    }

    /**
     * Called when the widget displaying this config entry is clicked; implementations must provide the user with some
     * means of editing the value, which accepts the {@code callback} object that is passed.
     *
     * @param clickedWidget the widget that was clicked to trigger this method;
     *                         may be useful to help position the edit controls you display
     * @param button the mouse button that triggered this
     * @param callback called when the editing GUI is either accepted or cancelled
     */
    public abstract void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback);

    public final Component getStringForGUI() {
        return Component.literal(String.valueOf(value));
    }

    public Component getStringForGUI(@Nullable T v) {
        return Component.literal(String.valueOf(v));
    }

    public String getPath() {
        if (group == null) return id;
        var p = group.getPath();
        return p.isEmpty() ? id : (p + '.' + id);
    }

    public String getNameKey() {
        return nameKey.isEmpty() ? getPath() : nameKey;
    }

    public EditableConfigValue<T> setNameKey(String key) {
        nameKey = key;
        return this;
    }

    public String getName() {
        return I18n.get(getNameKey());
    }

    public String getTooltip() {
        var k = getNameKey() + ".tooltip";
        return I18n.exists(k) ? I18n.get(k) : "";
    }

    public EditableConfigValue<T> setOrder(int o) {
        order = o;
        return this;
    }

    public boolean getCanEdit() {
        return canEdit;
    }

    public EditableConfigValue<T> setCanEdit(boolean e) {
        canEdit = e;
        return this;
    }

    public Icon<?> getIcon() {
        return getIcon(getValue());
    }

    public EditableConfigValue<T> setIcon(Icon<?> icon) {
        this.icon = icon;
        return this;
    }

    public Icon<?> getIcon(@Nullable T value) {
        return icon;
    }

    @Override
    public int compareTo(EditableConfigValue<T> o) {
        // sort by group, then ordering, then display name
        var cg = group.compareTo(o.group);
        if (cg != 0) return cg;
        int co = Integer.compare(order, o.order);
        return co != 0 ? co : getName().compareTo(o.getName());
    }

    public void applyValue() {
        setter.accept(value);
    }
}
