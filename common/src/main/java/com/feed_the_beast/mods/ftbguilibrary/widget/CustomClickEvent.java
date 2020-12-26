package com.feed_the_beast.mods.ftbguilibrary.widget;

import me.shedaniel.architectury.ForgeEvent;
import net.minecraft.resources.ResourceLocation;

/**
 * @author LatvianModder
 */
@ForgeEvent
// FIXME: Implement custom platform only annotations transform
// @Cancelable
public class CustomClickEvent
{
	private final ResourceLocation id;

	public CustomClickEvent(ResourceLocation id)
	{
		this.id = id;
	}

	public ResourceLocation getId()
	{
		return id;
	}
}