package dev.ftb.mods.ftblibrary.api.event.client;

import dev.ftb.mods.ftblibrary.sidebar.RegisteredSidebarButton;

import java.util.function.Consumer;

@FunctionalInterface
public interface SidebarButtonCreatedEvent extends Consumer<SidebarButtonCreatedEvent.Data> {
    record Data(RegisteredSidebarButton button) {
    }
}
