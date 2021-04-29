package dev.ftb.mods.ftblibrary.ui;

import me.shedaniel.architectury.event.Actor;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;
import net.minecraft.resources.ResourceLocation;

/**
 * @author LatvianModder
 */
public class CustomClickEvent {
	public static final Event<Actor<CustomClickEvent>> EVENT = EventFactory.createActorLoop(CustomClickEvent.class);

	private final ResourceLocation id;

	public CustomClickEvent(ResourceLocation id) {
		this.id = id;
	}

	public ResourceLocation getId() {
		return id;
	}
}