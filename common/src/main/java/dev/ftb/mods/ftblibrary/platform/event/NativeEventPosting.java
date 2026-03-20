package dev.ftb.mods.ftblibrary.platform.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public enum NativeEventPosting {
    INSTANCE;

    private final Map<TypedEvent<?,?>, Consumer<?>> consumers = new ConcurrentHashMap<>();
    private final Map<TypedEvent<?,?>, Function<?,?>> functions = new ConcurrentHashMap<>();

    public <T> void registerEvent(Class<T> dataClass, Consumer<T> dataHandler) {
        // Internal anonymous TypedEvent just for keying
        registerEvent(new TypedEvent<>(dataClass), dataHandler);
    }

    public <T> void registerEvent(TypedEvent<T, Void> event, Consumer<T> dataHandler) {
        consumers.put(event, dataHandler);
    }

    public <T, R> void registerEventWithResult(TypedEvent<T, R> event, Function<T, R> mapper) {
        functions.put(event, mapper);
    }

    public <T> void postEvent(TypedEvent<T, Void> event, T data) {
        if (consumers.get(event) instanceof Consumer<?> c) {
            //noinspection unchecked
            ((Consumer<T>) c).accept(data);
        } else {
            throw new IllegalArgumentException("unregistered event: " + event);
        }
    }

    public <T, R> R postEventWithResult(TypedEvent<T, R> event, T data) {
        if (functions.get(event) instanceof Function<?,?> f) {
            //noinspection unchecked
            return ((Function<T, R>) f).apply(data);
        } else {
            throw new IllegalArgumentException("unregistered event: " + event);
        }
    }
}
