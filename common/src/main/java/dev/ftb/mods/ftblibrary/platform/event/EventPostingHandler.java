package dev.ftb.mods.ftblibrary.platform.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public enum EventPostingHandler {
    INSTANCE;

    private final Map<Class<?>, Consumer<?>> consumers = new ConcurrentHashMap<>();
    private final Map<Class<?>, Function<?,?>> functions = new ConcurrentHashMap<>();

    public <T> void registerEvent(Class<T> dataClass, Consumer<T> dataHandler) {
        consumers.put(dataClass, dataHandler);
    }

    public <T,R> void registerEventWithResult(Class<T> dataClass, Function<T,R> dataHandler) {
        functions.put(dataClass, dataHandler);
    }

    @SuppressWarnings("unchecked")
    public <T> void postEvent(T data) {
        if (consumers.get(data.getClass()) instanceof Consumer<?> c) {
            // safe because all additions to the map are via registerEvent()
            //noinspection unchecked
            ((Consumer<T>) c).accept(data);
        } else {
            throw new IllegalArgumentException("unregistered data object: " + data.getClass().getName());
        }
    }

    public <T,R> R postEventWithResult(T data) {
        if (functions.get(data.getClass()) instanceof Function<?,?> f) {
            // safe because all additions to the map are via registerEventWithResult()
            //noinspection unchecked
            return ((Function<T,R>) f).apply(data);
        } else {
            throw new IllegalArgumentException("unregistered data object: " + data.getClass().getName());
        }
    }
}
