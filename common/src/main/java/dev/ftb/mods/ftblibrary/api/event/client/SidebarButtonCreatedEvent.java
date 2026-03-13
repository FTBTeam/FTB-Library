package dev.ftb.mods.ftblibrary.api.event.client;

import dev.ftb.mods.ftblibrary.sidebar.RegisteredSidebarButton;

@FunctionalInterface
public interface SidebarButtonCreatedEvent {
    void buttonCreated(Data data);

    record Data(RegisteredSidebarButton button) {
    }
}
