package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ConfigBoolean extends ConfigWithVariants<Boolean>
{
	public static final Color4I COLOR_TRUE = Color4I.rgb(0x33AA33);
	public static final Color4I COLOR_FALSE = Color4I.rgb(0xD52834);

	@Override
	public Color4I getColor(Boolean v)
	{
		return v ? COLOR_TRUE : COLOR_FALSE;
	}

	@Override
	public Boolean getIteration(Boolean v, boolean next)
	{
		return !v;
	}

	@Override
	public String getStringForGUI(@Nullable Boolean v)
	{
		return v == null ? "null" : v ? "True" : "False";
	}
}