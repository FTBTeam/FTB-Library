package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MathUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * @author LatvianModder
 */
public final class NameMap<E> implements Iterable<E>
{
	public static class ObjectProperties<T>
	{
		public String getName(T object)
		{
			return StringUtils.getID(object, StringUtils.FLAG_ID_ONLY_LOWERCASE | StringUtils.FLAG_ID_FIX);
		}

		public ITextComponent getDisplayName(T value)
		{
			return new StringTextComponent(getName(value));
		}

		public Color4I getColor(T object)
		{
			return Icon.EMPTY;
		}
	}

	@SafeVarargs
	public static <T> NameMap<T> create(T defaultValue, ObjectProperties<T> objectProperties, T... values)
	{
		List<T> list = new ArrayList<>(values.length);

		for (T e : values)
		{
			if (e != null)
			{
				list.add(e);
			}
		}

		if (list.isEmpty())
		{
			throw new IllegalStateException("Value list can't be empty!");
		}

		return new NameMap<>(defaultValue, objectProperties, list);
	}

	@SafeVarargs
	public static <T> NameMap<T> create(T defaultValue, T... values)
	{
		return create(defaultValue, new ObjectProperties<>(), values);
	}

	@SafeVarargs
	public static <T> NameMap<T> createWithName(T defaultValue, Function<T, ITextComponent> nameGetter, T... values)
	{
		return create(defaultValue, new ObjectProperties<T>()
		{
			@Override
			public ITextComponent getDisplayName(T value)
			{
				return nameGetter.apply(value);
			}
		}, values);
	}

	@SafeVarargs
	public static <T> NameMap<T> createWithNameAndColor(T defaultValue, Function<T, ITextComponent> nameGetter, Function<T, Color4I> colorGetter, T... values)
	{
		return create(defaultValue, new ObjectProperties<T>()
		{
			@Override
			public ITextComponent getDisplayName(T value)
			{
				return nameGetter.apply(value);
			}

			@Override
			public Color4I getColor(T object)
			{
				return colorGetter.apply(object);
			}
		}, values);
	}

	@SafeVarargs
	public static <T> NameMap<T> createWithTranslation(T defaultValue, Function<T, String> nameGetter, T... values)
	{
		return create(defaultValue, new ObjectProperties<T>()
		{
			@Override
			public ITextComponent getDisplayName(T value)
			{
				return new TranslationTextComponent(nameGetter.apply(value));
			}
		}, values);
	}

	@SafeVarargs
	public static <T> NameMap<T> createWithBaseTranslationKey(T defaultValue, String baseTranslationKey, T... values)
	{
		return create(defaultValue, new ObjectProperties<T>()
		{
			@Override
			public ITextComponent getDisplayName(T value)
			{
				return new TranslationTextComponent(baseTranslationKey + "." + getName(value));
			}
		}, values);
	}

	private final ObjectProperties<E> objectProperties;
	public final E defaultValue;
	public final Map<String, E> map;
	public final List<String> keys;
	public final List<E> values;

	private NameMap(E def, ObjectProperties<E> ng, List<E> v)
	{
		objectProperties = ng;
		values = v;

		Map<String, E> map0 = new LinkedHashMap<>(size());

		for (E value : values)
		{
			map0.put(getName(value), value);
		}

		map = Collections.unmodifiableMap(map0);
		keys = Collections.unmodifiableList(new ArrayList<>(map.keySet()));
		defaultValue = get(getName(def));
	}

	private NameMap(E def, NameMap<E> n)
	{
		objectProperties = n.objectProperties;
		map = n.map;
		keys = n.keys;
		values = n.values;
		defaultValue = get(getName(def));
	}

	public String getName(E value)
	{
		return objectProperties.getName(value);
	}

	public ITextComponent getDisplayName(E value)
	{
		return objectProperties.getDisplayName(value);
	}

	public Color4I getColor(E value)
	{
		return objectProperties.getColor(value);
	}

	public NameMap<E> withDefault(E def)
	{
		if (def == defaultValue)
		{
			return this;
		}

		return new NameMap<>(def, this);
	}

	public int size()
	{
		return values.size();
	}

	public E get(@Nullable String s)
	{
		if (s == null || s.isEmpty() || s.charAt(0) == '-')
		{
			return defaultValue;
		}
		else
		{
			E e = map.get(s);
			return e == null ? defaultValue : e;
		}
	}

	@Nullable
	public E getNullable(@Nullable String s)
	{
		if (s == null || s.isEmpty() || s.charAt(0) == '-')
		{
			return null;
		}
		else
		{
			return map.get(s);
		}
	}

	public E get(int index)
	{
		return index < 0 || index >= size() ? defaultValue : values.get(index);
	}

	public E offset(E value, int index)
	{
		return get(MathUtils.mod(getIndex(value) + index, size()));
	}

	public E getNext(E value)
	{
		return offset(value, 1);
	}

	public E getPrevious(E value)
	{
		return offset(value, -1);
	}

	public int getIndex(E e)
	{
		return values.indexOf(e);
	}

	public int getStringIndex(String s)
	{
		return getIndex(map.get(s));
	}

	public E getRandom(Random rand)
	{
		return values.get(rand.nextInt(size()));
	}

	@Override
	public Iterator<E> iterator()
	{
		return values.iterator();
	}

	public void write(PacketBuffer data, E object)
	{
		data.writeVarInt(getIndex(object));
	}

	public E read(PacketBuffer data)
	{
		return get(data.readVarInt());
	}
}