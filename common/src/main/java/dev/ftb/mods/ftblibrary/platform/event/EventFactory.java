package dev.ftb.mods.ftblibrary.platform.event;

import com.google.common.collect.MapMaker;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class EventFactory {
    private static final Set<ArrayBackedEvent<?>> ARRAY_BACKED_EVENTS
            = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    public static <T> Event<T> createEvent(Class<? super T> listenerType, Function<T[], T> invokerFactory) {
        ArrayBackedEvent<T> event = new ArrayBackedEvent<>(listenerType, invokerFactory);
        ARRAY_BACKED_EVENTS.add(event);
        return event;
    }

    public static void invalidate() {
        for (ArrayBackedEvent<?> event : ARRAY_BACKED_EVENTS) {
            event.invoker = null;
        }
    }
}
