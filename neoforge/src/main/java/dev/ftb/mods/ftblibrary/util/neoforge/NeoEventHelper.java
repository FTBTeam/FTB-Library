package dev.ftb.mods.ftblibrary.util.neoforge;

import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Function;

public class NeoEventHelper {
    public static <T, R extends Event> void registerNeoEventPoster(IEventBus bus, Class<T> cls, Function<T, R> factory) {
        EventPostingHandler.INSTANCE.registerEvent(cls, data -> bus.post(factory.apply(data)));
    }
}
