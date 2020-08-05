package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ConfigBoolean extends ConfigWithVariants<Boolean>
{
	public static final StringTextComponent TRUE_TEXT = new StringTextComponent("True");
	public static final StringTextComponent FALSE_TEXT = new StringTextComponent("False");

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
	public ITextComponent getStringForGUI(@Nullable Boolean v)
	{
		return v == null ? NULL_TEXT : v ? TRUE_TEXT : FALSE_TEXT;
	}

	@Override
	public Icon getIcon(@Nullable Boolean v)
	{
		return v == null || !v ? GuiIcons.ACCEPT_GRAY : GuiIcons.ACCEPT;
	}
}