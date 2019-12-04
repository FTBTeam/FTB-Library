package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;

/**
 * @author LatvianModder
 */
public class ConfigBoolean extends ConfigWithVariants<Boolean>
{
	public static final Color4I COLOR_TRUE = Color4I.rgb(0x33AA33);
	public static final Color4I COLOR_FALSE = Color4I.rgb(0xD52834);

	@Override
	public Color4I getColor(Boolean value)
	{
		return value ? COLOR_TRUE : COLOR_FALSE;
	}

	@Override
	public Boolean getIteration(Boolean value, boolean next)
	{
		return !value;
	}

	@Override
	public String getStringForGUI(Boolean value)
	{
		return value ? "True" : "False";
	}
}