package com.feed_the_beast.mods.ftbguilibrary.sidebar;

import net.minecraftforge.eventbus.api.Event;

/**
 * @author LatvianModder
 */
public class SidebarButtonCreatedEvent extends Event
{
	private final SidebarButton button;

	public SidebarButtonCreatedEvent(SidebarButton b)
	{
		button = b;
	}

	public SidebarButton getButton()
	{
		return button;
	}
}