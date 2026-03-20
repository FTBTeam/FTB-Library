package dev.ftb.mods.ftblibrary.platform.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public enum NativeEventPosting {
    INSTANCE;

    private final Map<Class<?>, Consumer<?>> consumers = new ConcurrentHashMap<>();
    private final Map<TypedEvent<?,?>, Function<?,?>> functions = new ConcurrentHashMap<>();

    public static NativeEventPosting get() {
        return INSTANCE;
    }

    public <T> void registerEvent(Class<T> cls, Consumer<T> dataHandler) {
        consumers.put(cls, dataHandler);
    }

    public <T, R> void registerEventWithResult(TypedEvent<T, R> event, Function<T, R> mapper) {
        functions.put(event, mapper);
    }

    public <T> void postEvent(T data) {
        if (consumers.get(data.getClass()) instanceof Consumer<?> c) {
            //noinspection unchecked
            ((Consumer<T>) c).accept(data);
        } else {
            throw new IllegalArgumentException("unregistered event: " + data.getClass().getName());
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
