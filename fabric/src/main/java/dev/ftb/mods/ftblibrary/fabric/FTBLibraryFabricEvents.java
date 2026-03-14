package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.api.event.client.RegisterCustomColorEvent;
import dev.ftb.mods.ftblibrary.api.event.client.SidebarButtonCreatedEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Arrays;

public class FTBLibraryFabricEvents {
    public static Event<CustomClickEvent> CUSTOM_CLICK = EventFactory.createArrayBacked(CustomClickEvent.class,
            callbacks -> data ->
                    Arrays.stream(callbacks).anyMatch(event -> event.onClicked(data))
    );

    public static Event<RegisterCustomColorEvent> REGISTER_CUSTOM_COLOR = EventFactory.createArrayBacked(RegisterCustomColorEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.register(data))
    );

    public static Event<SidebarButtonCreatedEvent> SIDEBAR_BUTTON_CREATED = EventFactory.createArrayBacked(SidebarButtonCreatedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.buttonCreated(data))
    );
}
