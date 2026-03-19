package dev.ftb.mods.ftblibrary.platform.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public enum NativeEventPosting {
    INSTANCE;

    private final Map<Class<?>, Consumer<?>> consumers = new ConcurrentHashMap<>();
    private final Map<Class<?>, Function<?,?>> functions = new ConcurrentHashMap<>();

    public <T> void registerEvent(Class<T> dataClass, Consumer<T> dataHandler) {
        consumers.put(dataClass, dataHandler);
    }

    public <T,R> void registerEventWithResult(TypedEvent<T, R> typedEvent, Function<T,R> mapper) {
        functions.put(typedEvent.dataClass(), mapper);
    }

    public <T> void postEvent(T data) {
        if (consumers.get(data.getClass()) instanceof Consumer<?> c) {
            // safe because all additions to the map are via registerEvent()
            //noinspection unchecked
            ((Consumer<T>) c).accept(data);
        } else {
            throw new IllegalArgumentException("unregistered event data object: " + data.getClass().getName());
        }
    }

    public <T,R> R postEventWithResult(TypedEvent<T,R> event, T data) {
        if (functions.get(event.dataClass()) instanceof Function<?,?> f) {
            // safe because all additions to the map are via registerEventWithResult()
            //noinspection unchecked
            return ((Function<T,R>) f).apply(data);
        } else {
            throw new IllegalArgumentException("unregistered event data object: " + data.getClass().getName());
        }
    }
}
