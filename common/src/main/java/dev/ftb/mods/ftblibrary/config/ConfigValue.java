package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public abstract class ConfigValue<T> implements Comparable<ConfigValue<T>> {
	public static final TextComponent NULL_TEXT = new TextComponent("null");

	public ConfigGroup group;
	public T value;
	public Consumer<T> setter;
	public T defaultValue;

	public String id = "";
	private int order = 0;
	private String nameKey = "";
	private Icon icon = Icons.SETTINGS;
	private boolean canEdit = true;

	public ConfigValue<T> init(ConfigGroup g, String i, @Nullable T v, Consumer<T> c, @Nullable T def) {
		group = g;
		id = i;
		value = v == null ? null : copy(v);
		setter = c;
		defaultValue = def;
		order = g.getValues().size();
		return this;
	}

	public final boolean setCurrentValue(@Nullable T v) {
		if (!isEqual(value, v)) {
			value = v;
			return true;
		}

		return false;
	}

	public boolean isEqual(@Nullable T v1, @Nullable T v2) {
		return Objects.equals(v1, v2);
	}

	public T copy(T value) {
		return value;
	}

	public Color4I getColor(@Nullable T v) {
		return Color4I.GRAY;
	}

	public void addInfo(TooltipList list) {
		list.add(info("Default", getStringForGUI(defaultValue)));
	}

	protected static Component info(String key) {
		return new TextComponent(key + ":").withStyle(ChatFormatting.AQUA);
	}

	public static Component info(String key, Object value) {
		Component c = value instanceof Component ? (Component) value : new TextComponent(String.valueOf(value));
		return new TextComponent("").append((new TextComponent(key + ": ").withStyle(ChatFormatting.AQUA))).append(c);
	}

	public abstract void onClicked(MouseButton button, ConfigCallback callback);

	public Component getStringForGUI(@Nullable T v) {
		return new TextComponent(String.valueOf(v));
	}

	public String getPath() {
		String p = group.getPath();
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
		String k = getNameKey() + ".tooltip";
		return I18n.exists(k) ? I18n.get(k) : "";
	}

	public ConfigValue<T> setOrder(int o) {
		order = o;
		return this;
	}

	public ConfigValue<T> setCanEdit(boolean e) {
		canEdit = e;
		return this;
	}

	public boolean getCanEdit() {
		return canEdit;
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
		int i = group.getPath().compareToIgnoreCase(o.group.getPath());

		if (i == 0) {
			i = Integer.compare(order, o.order);
		}

		return i;
	}
}