package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class ConfigValue<T> implements Comparable<ConfigValue<T>> {
    public static final Component NULL_TEXT = Component.literal("null");
    public String id = "";
    protected T value;
    protected T defaultValue;
    private ConfigGroup group;
    private Consumer<T> setter;
    private int order = 0;
    private String nameKey = "";
    private Icon icon = Icons.SETTINGS;
    private boolean canEdit = true;

    protected static Component info(String key) {
        return Component.literal(key + ":").withStyle(ChatFormatting.AQUA);
    }

    public static Component info(String key, Object value) {
        var c = value instanceof Component ? (Component) value : Component.literal(String.valueOf(value));
        return Component.literal("").append((Component.literal(key + ": ").withStyle(ChatFormatting.AQUA))).append(c);
    }

    /**
     * Initialise this config value; called when it's added to a config group with
     * {@link ConfigGroup#add(String, ConfigValue, Object, Consumer, Object)}
     *
     * @param group the group being added to
     * @param id a unique id for this value
     * @param value the initial value
     * @param setter a consumer to be called to apply changes to the value
     * @param defaultValue the default value
     * @return the initialised config value
     */
    public ConfigValue<T> init(ConfigGroup group, String id, @Nullable T value, Consumer<T> setter, @Nullable T defaultValue) {
        this.group = group;
        this.id = id;
        this.value = value == null ? null : copy(value);
        this.setter = setter;
        this.defaultValue = defaultValue;
        this.order = group.getValues().size();
        return this;
    }

    public final boolean setCurrentValue(@Nullable T v) {
        if (!isEqual(value, v)) {
            value = v;
            return true;
        }

        return false;
    }

    public ConfigGroup getGroup() {
        return group;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isEqual(@Nullable T v1, @Nullable T v2) {
        return Objects.equals(v1, v2);
    }

    public T copy(T value) {
        return value;
    }

    public final Color4I getColor() {
        return getColor(value);
    }

    public Color4I getColor(@Nullable T v) {
        return Color4I.GRAY;
    }

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

    public ConfigValue<T> setNameKey(String key) {
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

    public ConfigValue<T> setOrder(int o) {
        order = o;
        return this;
    }

    public boolean getCanEdit() {
        return canEdit;
    }

    public ConfigValue<T> setCanEdit(boolean e) {
        canEdit = e;
        return this;
    }

    public Icon getIcon() {
        return getIcon(getValue());
    }

    public ConfigValue<T> setIcon(Icon i) {
        icon = i;
        return this;
    }

    public Icon getIcon(@Nullable T v) {
        return icon;
    }

    @Override
    public int compareTo(ConfigValue<T> o) {
        var i = group.compareTo(o.group);
        return i == 0 ? Integer.compare(order, o.order) : i;
    }

    public void applyValue() {
        setter.accept(value);
    }
}
