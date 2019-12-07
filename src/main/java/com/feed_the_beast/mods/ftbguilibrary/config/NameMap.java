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
import java.util.Arrays;
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
	public static final class Builder<T>
	{
		private final T defaultValue;
		private final List<T> values;

		private Function<T, String> idProvider = t -> StringUtils.getID(t, StringUtils.FLAG_ID_FIX | StringUtils.FLAG_ID_ONLY_LOWERCASE);
		private Function<T, ITextComponent> nameProvider = t -> new StringTextComponent(idProvider.apply(t));
		private Function<T, Color4I> colorProvider = t -> Icon.EMPTY;
		private Function<T, Icon> iconProvider = t -> Icon.EMPTY;

		private Builder(T def, List<T> v)
		{
			defaultValue = def;
			values = v;
		}

		public Builder<T> id(Function<T, String> p)
		{
			idProvider = p;
			return this;
		}

		public Builder<T> name(Function<T, ITextComponent> p)
		{
			nameProvider = p;
			return this;
		}

		public Builder<T> nameKey(Function<T, String> p)
		{
			return name(v -> new TranslationTextComponent(p.apply(v)));
		}

		public Builder<T> color(Function<T, Color4I> p)
		{
			colorProvider = p;
			return this;
		}

		public Builder<T> icon(Function<T, Icon> p)
		{
			iconProvider = p;
			return this;
		}

		public NameMap<T> create()
		{
			return new NameMap<>(this);
		}
	}

	public static <T> NameMap.Builder<T> of(T defaultValue, List<T> values)
	{
		return new Builder<>(defaultValue, values);
	}

	public static <T> NameMap.Builder<T> of(T defaultValue, T[] values)
	{
		return of(defaultValue, Arrays.asList(values));
	}

	private final Builder<E> builder;
	public final E defaultValue;
	public final Map<String, E> map;
	public final List<String> keys;
	public final List<E> values;

	private NameMap(Builder<E> b)
	{
		builder = b;
		values = Collections.unmodifiableList(b.values);

		Map<String, E> map0 = new LinkedHashMap<>(size());

		for (E value : values)
		{
			map0.put(getName(value), value);
		}

		map = Collections.unmodifiableMap(map0);
		keys = Collections.unmodifiableList(new ArrayList<>(map.keySet()));
		defaultValue = get(getName(builder.defaultValue));
	}

	private NameMap(E def, NameMap<E> n)
	{
		builder = n.builder;
		map = n.map;
		keys = n.keys;
		values = n.values;
		defaultValue = get(getName(def));
	}

	public String getName(E value)
	{
		return builder.idProvider.apply(value);
	}

	public ITextComponent getDisplayName(E value)
	{
		return builder.nameProvider.apply(value);
	}

	public Color4I getColor(E value)
	{
		return builder.colorProvider.apply(value);
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
		E value = getNullable(s);
		return value == null ? defaultValue : value;
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

	public Icon getIcon(E v)
	{
		return Icon.EMPTY;
	}
}