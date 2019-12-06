package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class ConfigNumber<T extends Number> extends ConfigFromString<T>
{
	public static final Color4I COLOR = Color4I.rgb(0xAA5AE8);

	public final T min;
	public final T max;

	public ConfigNumber(T mn, T mx)
	{
		min = mn;
		max = mx;
	}

	@Override
	public Color4I getColor(@Nullable T v)
	{
		return COLOR;
	}

	@Override
	public String getStringForGUI(@Nullable T v)
	{
		return v == null ? "null" : StringUtils.formatDouble(v.doubleValue(), true);
	}
}