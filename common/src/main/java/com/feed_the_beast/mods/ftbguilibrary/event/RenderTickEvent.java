package com.feed_the_beast.mods.ftbguilibrary.event;

import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;

public interface RenderTickEvent
{
	Event<Pre> PRE = EventFactory.createLoop(Pre.class);

	void tick();

	interface Pre extends RenderTickEvent
	{
	}
}
