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
public abstract class ConfigValue<T>
{
	public ConfigGroup group;
	public T initial;
	public T current;
	public Consumer<T> callback;
	public T defaultValue;

	public String id = "";
	public int order = 0;
	public String translationKey = "";
	public Icon icon = Icon.EMPTY;
	public boolean hidden = false;
	public boolean canEdit = true;

	public ConfigValue<T> init(ConfigGroup g, String i, T value, Consumer<T> c, T def)
	{
		group = g;
		id = i;
		initial = value;
		current = copy(value);
		callback = c;
		defaultValue = def;
		return this;
	}

	public <E extends ConfigValue<T>> E init(T value, Consumer<T> c, T def)
	{
		ConfigGroup group = new ConfigGroup("unknown");
		group.add("unknown", this, value, c, def);
		return (E) this;
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

	public String getTranslationKey()
	{
		return translationKey.isEmpty() ? getPath() : translationKey;
	}

	public String getName()
	{
		return I18n.format(getTranslationKey());
	}

	public String getTooltip()
	{
		String k = getTranslationKey();
		return I18n.hasKey(k) ? I18n.format(k) : "";
	}
}