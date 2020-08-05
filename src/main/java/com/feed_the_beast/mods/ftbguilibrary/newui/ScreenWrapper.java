package com.feed_the_beast.mods.ftbguilibrary.newui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

/**
 * @author LatvianModder
 */
public class ScreenWrapper extends Screen
{
	public final UI ui;

	public ScreenWrapper(String id, ITextComponent title)
	{
		super(title);
		ui = new UI(id, title);
	}
}
