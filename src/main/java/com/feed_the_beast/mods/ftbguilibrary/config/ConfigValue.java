package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public abstract class ConfigValue<T> implements Comparable<ConfigValue<T>>
{
	public ConfigGroup group;
	public T initial;
	public T current;
	public Consumer<T> callback;
	public T defaultValue;

	public String id = "";
	private int order = 0;
	private String nameKey = "";
	private Icon icon = Icon.EMPTY;
	private boolean canEdit = true;

	public ConfigValue<T> init(ConfigGroup g, String i, T value, Consumer<T> c, T def)
	{
		group = g;
		id = i;
		initial = value;
		current = copy(value);
		callback = c;
		defaultValue = def;
		order = g.getValues().size();
		return this;
	}

	public final boolean setCurrentValue(T value)
	{
		if (isValid(value) && !isEqual(current, value))
		{
			current = value;
			return true;
		}

		return false;
	}

	public final boolean reset()
	{
		return setCurrentValue(initial);
	}

	public final boolean isDefault()
	{
		return isEqual(current, defaultValue);
	}

	public boolean isValid(T value)
	{
		return true;
	}

	public boolean isEqual(T value1, T value2)
	{
		return value1.equals(value2);
	}

	public T copy(T value)
	{
		return value;
	}

	public Color4I getColor(T value)
	{
		return Color4I.GRAY;
	}

	public void addInfo(List<String> list)
	{
		list.add(TextFormatting.AQUA + "Default: " + TextFormatting.RESET + getStringForGUI(defaultValue));
	}

	public abstract void onClicked(MouseButton button, Runnable callback);

	public String getStringForGUI(T value)
	{
		return value.toString();
	}

	public boolean isEmpty(T value)
	{
		return false;
	}

	public String getPath()
	{
		String p = group.getPath();
		return p.isEmpty() ? id : (id + '.' + p);
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