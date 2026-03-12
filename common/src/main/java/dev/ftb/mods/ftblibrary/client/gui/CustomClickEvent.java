package dev.ftb.mods.ftblibrary.client.gui;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventFactory;
import net.minecraft.resources.Identifier;

public record CustomClickEvent(Identifier id) {
    public static final Event<EventActor<CustomClickEvent>> EVENT = EventFactory.createEventActorLoop(CustomClickEvent.class);
}
