package dev.ftb.mods.ftblibrary.ui;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventFactory;
import net.minecraft.resources.ResourceLocation;


public record CustomClickEvent(ResourceLocation id) {
    public static final Event<EventActor<CustomClickEvent>> EVENT = EventFactory.createEventActorLoop(CustomClickEvent.class);
}
