package dev.ftb.mods.ftblibrary.api.sidebar;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftblibrary.sidebar.RegisteredSidebarButton;

import java.util.function.Consumer;

// TODO currently broken for neoforge, uncomment when there's a fix in architectury
//@ForgeEvent
public class SidebarButtonCreatedEvent {
    public static final Event<Consumer<SidebarButtonCreatedEvent>> EVENT = EventFactory.createConsumerLoop(SidebarButtonCreatedEvent.class);

    private final RegisteredSidebarButton button;

    public SidebarButtonCreatedEvent(RegisteredSidebarButton button) {
        this.button = button;
    }

    public RegisteredSidebarButton getButton() {
        return button;
    }
}
