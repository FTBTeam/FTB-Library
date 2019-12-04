package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;

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
	public abstract boolean isValid(T value);

	@Override
	public Color4I getColor(T value)
	{
		return COLOR;
	}

	@Override
	public String getStringForGUI(T value)
	{
		return StringUtils.formatDouble(value.doubleValue(), true);
	}
}