package com.feed_the_beast.mods.ftbguilibrary.newui;

import net.minecraft.util.text.ITextComponent;

/**
 * @author LatvianModder
 */
public class UI extends Panel
{
	public final ITextComponent title;

	public UI(String i, ITextComponent t)
	{
		ui = this;
		id = i;
		title = t;
	}
}
