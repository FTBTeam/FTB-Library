package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public abstract class ConfigValue<T> implements Comparable<ConfigValue<T>>
{
	public ConfigGroup group;
	public T value;
	public Consumer<T> setter;
	public T defaultValue;

	public String id = "";
	private int order = 0;
	private String nameKey = "";
	private Icon icon = GuiIcons.SETTINGS;
	private boolean canEdit = true;

	public ConfigValue<T> init(ConfigGroup g, String i, @Nullable T v, Consumer<T> c, @Nullable T def)
	{
		group = g;
		id = i;
		value = v == null ? null : copy(v);
		setter = c;
		defaultValue = def;
		order = g.getValues().size();
		return this;
	}

	public final boolean setCurrentValue(T v)
	{
		if (!isEqual(value, v))
		{
			value = v;
			return true;
		}

		return false;
	}

	public boolean isEqual(T v1, T v2)
	{
		return Objects.equals(v1, v2);
	}

	public T copy(T value)
	{
		return value;
	}

	public Color4I getColor(@Nullable T v)
	{
		return Color4I.GRAY;
	}

	public void addInfo(List<String> list)
	{
		list.add(TextFormatting.AQUA + "Default: " + TextFormatting.RESET + getStringForGUI(defaultValue));
	}

	public abstract void onClicked(MouseButton button, ConfigCallback callback);

	public String getStringForGUI(@Nullable T v)
	{
		return String.valueOf(v);
	}

	public String getPath()
	{
		String p = group.getPath();
		return p.isEmpty() ? id : (p + '.' + id);
	}

	public String getNameKey()
	{
		return nameKey.isEmpty() ? getPath() : nameKey;
	}

	public ConfigValue<T> setNameKey(String key)
	{
		nameKey = key;
		return this;
	}

	public String getName()
	{
		return I18n.format(getNameKey());
	}

	public String getTooltip()
	{
		String k = getNameKey();
		return I18n.hasKey(k) ? I18n.format(k) : "";
	}

	public ConfigValue<T> setOrder(int o)
	{
		order = o;
		return this;
	}

	public ConfigValue<T> setCanEdit(boolean e)
	{
		canEdit = e;
		return this;
	}

	public boolean getCanEdit()
	{
		return canEdit;
	}

	public ConfigValue<T> setIcon(Icon i)
	{
		icon = i;
		return this;
	}

	public Icon getIcon(T value)
	{
		return icon;
	}

	@Override
	public int compareTo(ConfigValue<T> o)
	{
		int i = group.getPath().compareToIgnoreCase(o.group.getPath());

		if (i == 0)
		{
			i = Integer.compare(order, o.order);
		}

		return i;
	}
}