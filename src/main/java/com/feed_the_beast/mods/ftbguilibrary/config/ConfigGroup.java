package com.feed_the_beast.mods.ftbguilibrary.config;

import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ConfigGroup
{
	public final String id;
	public ConfigGroup parent;
	private final Map<String, ConfigValue> values;
	private final Map<String, ConfigGroup> groups;
	public Runnable savedCallback;

	public ConfigGroup(String i)
	{
		id = i;
		values = new LinkedHashMap<>();
		groups = new LinkedHashMap<>();
		savedCallback = null;
	}

	public String getTranslationKey()
	{
		return getPath();
	}

	public String getName()
	{
		return I18n.format(getTranslationKey());
	}

	public String getTooltip()
	{
		String t = getTranslationKey() + ".tooltip";
		return I18n.hasKey(t) ? I18n.format(t) : "";
	}

	public ConfigGroup getGroup(String id)
	{
		int index = id.indexOf('.');

		if (index == -1)
		{
			ConfigGroup g = groups.get(id);

			if (g == null)
			{
				g = new ConfigGroup(id);
				g.parent = this;
				groups.put(g.id, g);
			}

			return g;
		}

		return getGroup(id.substring(0, index)).getGroup(id.substring(index + 1));
	}

	public <T, CV extends ConfigValue<T>> CV add(String id, CV type, T value, Consumer<T> callback, T defaultValue)
	{
		type.init(this, id, value, callback, defaultValue);
		values.put(id, type);
		return type;
	}

	public ConfigBoolean addBool(String id, boolean value, Consumer<Boolean> setter, boolean def)
	{
		return add(id, new ConfigBoolean(), value, setter, def);
	}

	public ConfigInt addInt(String id, int value, Consumer<Integer> setter, int def, int min, int max)
	{
		return add(id, new ConfigInt(min, max), value, setter, def);
	}

	public ConfigLong addInt(String id, long value, Consumer<Long> setter, long def, long min, long max)
	{
		return add(id, new ConfigLong(min, max), value, setter, def);
	}

	public ConfigDouble addInt(String id, double value, Consumer<Double> setter, double def, double min, double max)
	{
		return add(id, new ConfigDouble(min, max), value, setter, def);
	}

	public ConfigString addString(String id, String value, Consumer<String> setter, String def, @Nullable Pattern pattern)
	{
		return add(id, new ConfigString(pattern), value, setter, def);
	}

	public ConfigString addString(String id, String value, Consumer<String> setter, String def)
	{
		return addString(id, value, setter, def, null);
	}

	public <E> ConfigEnum<E> addEnum(String id, E value, Consumer<E> setter, NameMap<E> nameMap, E def)
	{
		return add(id, new ConfigEnum<>(nameMap), value, setter, def);
	}

	public <E> ConfigEnum<E> addEnum(String id, E value, Consumer<E> setter, NameMap<E> nameMap)
	{
		return addEnum(id, value, setter, nameMap, nameMap.defaultValue);
	}

	public <E, CV extends ConfigValue<E>> ConfigList<E, CV> addList(String id, List<E> c, CV type, E def)
	{
		type.defaultValue = def;
		return add(id, new ConfigList<>(type), c, t -> {
			c.clear();
			c.addAll(t);
		}, Collections.emptyList());
	}

	public ConfigEnum<Tristate> addTristate(String id, Tristate value, Consumer<Tristate> setter, Tristate def)
	{
		return addEnum(id, value, setter, Tristate.NAME_MAP, def);
	}

	public ConfigEnum<Tristate> addTristate(String id, Tristate value, Consumer<Tristate> setter)
	{
		return addTristate(id, value, setter, Tristate.DEFAULT);
	}

	public final Collection<ConfigValue> getValues()
	{
		return values.values();
	}

	public final Collection<ConfigGroup> getGroups()
	{
		return groups.values();
	}

	public String getPath()
	{
		if (parent == null)
		{
			return id;
		}

		return parent.getPath() + '.' + id;
	}

	public boolean reset()
	{
		boolean b = false;

		for (ConfigValue value : values.values())
		{
			b = value.reset() || b;
		}

		for (ConfigGroup group : groups.values())
		{
			b = group.reset() || b;
		}

		return b;
	}

	public void save()
	{
		for (ConfigValue value : values.values())
		{
			value.callback.accept(value.current);
		}

		for (ConfigGroup group : groups.values())
		{
			group.save();
		}

		if (savedCallback != null)
		{
			savedCallback.run();
		}
	}
}