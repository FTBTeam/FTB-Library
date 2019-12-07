package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ConfigBoolean extends ConfigWithVariants<Boolean>
{
	@Override
	public Color4I getColor(@Nullable Boolean v)
	{
		return v == null || !v ? Tristate.FALSE.color : Tristate.TRUE.color;
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

	@Override
	public Icon getIcon(@Nullable Boolean v)
	{
		return v == null || !v ? GuiIcons.ACCEPT_GRAY : GuiIcons.ACCEPT;
	}
}