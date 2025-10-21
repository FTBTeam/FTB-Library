package dev.ftb.mods.ftblibrary.api.color;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.network.chat.TextColor;

import java.util.Map;
import java.util.function.Consumer;

public record RegisterCustomColorEvent(
            Map<String, TextColor> colors
) {
    public static final Event<Consumer<RegisterCustomColorEvent>> EVENT = EventFactory.createConsumerLoop(RegisterCustomColorEvent.class);

    public void register(String id, TextColor color) {
        colors.put(id, color);
    }
}
