package dev.ftb.mods.ftblibrary.ui;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventFactory;
import net.minecraft.resources.ResourceLocation;

/**
 * @author LatvianModder
 */
public class CustomClickEvent {
	public static final Event<EventActor<CustomClickEvent>> EVENT = EventFactory.createEventActorLoop(CustomClickEvent.class);

	private final ResourceLocation id;

	public CustomClickEvent(ResourceLocation id) {
		this.id = id;
	}

	public ResourceLocation getId() {
		return id;
	}
}