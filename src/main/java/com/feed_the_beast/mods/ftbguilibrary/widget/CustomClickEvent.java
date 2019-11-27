package com.feed_the_beast.mods.ftbguilibrary.widget;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author LatvianModder
 */
@Cancelable
public class CustomClickEvent extends Event
{
	private final ResourceLocation id;

	public CustomClickEvent(ResourceLocation _id)
	{
		id = _id;
	}

	public ResourceLocation getId()
	{
		return id;
	}
}