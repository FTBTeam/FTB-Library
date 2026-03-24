package dev.ftb.mods.ftblibrary.platform.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/// Allows platform-independent posting of our own mods' "events", by mapping an event data object (typically a
/// record) to a `Consumer` or `Function` which handles posting the corresponding native NeoForge/Fabric event.
public enum NativeEventPosting {
    INSTANCE;

    private final Map<Class<?>, Consumer<?>> consumers = new ConcurrentHashMap<>();
    private final Map<TypedEvent<?,?>, Function<?,?>> functions = new ConcurrentHashMap<>();

    public static NativeEventPosting get() {
        return INSTANCE;
    }

    /// Register a `Consumer` event poster; this must be called in platform-dependent code, since the consumer handles
    /// posting of the native platform event. `Consumer` posters are used for events which do not return any data.
    ///
    /// @param cls the class of the data structure which will later be posted via [#postEvent(Object)]
    /// @param dataHandler the consumer object
    /// @param <T> type of the data to be posted
    public <T> void registerEvent(Class<T> cls, Consumer<T> dataHandler) {
        consumers.put(cls, dataHandler);
    }

    /// Register a `Function` event poster; this must be called in platform-dependent code, since the consumer handles
    /// posting of the native platform event. `Function` posters are used for events which return a result of some kind;
    /// the type of the result is defined in the [TypedEvent] parameter.
    ///
    /// @param eventType the class and event return type of the data structure which will later
    /// @param mapper the function which takes the typed event and posts a corresponding native event,
    /// and returns the event result
    /// @param <T> type of the data to be posted
    public <T, R> void registerEventWithResult(TypedEvent<T, R> eventType, Function<T, R> mapper) {
        functions.put(eventType, mapper);
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
