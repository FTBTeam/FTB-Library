package com.feed_the_beast.mods.ftbguilibrary.newui;

import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class UI extends Panel
{
	public final Component title;

	public UI(String i, Component t)
	{
		ui = this;
		id = i;
		title = t;
	}
}
