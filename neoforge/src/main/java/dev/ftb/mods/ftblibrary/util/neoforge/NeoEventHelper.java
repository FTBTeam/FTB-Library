package dev.ftb.mods.ftblibrary.util.neoforge;

import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftblibrary.platform.event.TypedEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Function;

public class NeoEventHelper {
    public static <T, R extends Event> void registerNeoEventPoster(IEventBus bus, Class<T> cls, Function<T, R> factory) {
        NativeEventPosting.INSTANCE.registerEvent(cls, data -> bus.post(factory.apply(data)));
    }

    public static <T, R extends Event & ICancellableEvent> void registerCancellableNeoEventPoster(IEventBus bus, TypedEvent<T, Boolean> type, Function<T, R> factory) {
        NativeEventPosting.INSTANCE.registerEventWithResult(type, data -> {
            R event = factory.apply(data);
            bus.post(event);
            return !event.isCanceled();
        });
    }
}
