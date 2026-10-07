package dev.ftb.mods.ftblibrary.platform.event;

/// Represents an event type with an outcome of a specific type; used for registering events with results with
/// [NativeEventPosting#registerEventWithResult(TypedEvent, java.util.function.Function)]
/// and posting them with [NativeEventPosting#postEventWithResult(TypedEvent, Object)].
///
/// Common result type are [Boolean] indicating simple success or failure, and
/// [dev.ftb.mods.ftblibrary.util.result.DataOutcome], a utility class which can be used to carry a result payload
/// along with success/fail/pass result.
///
/// @param dataClass class for the data object that the event holds
/// @param <T> data object type
/// @param <R> the type of the result that listeners of the event will return
public record TypedEvent<T, R>(Class<T> dataClass) {

    public static <T, R> TypedEvent<T, R> of(Class<T> dataClass) {
        return new TypedEvent<>(dataClass);
    }

    public static <T> TypedEvent<T, Boolean> ofBoolean(Class<T> dataClass) {
        return new TypedEvent<>(dataClass);
    }

    public static <T> TypedEvent<T, Void> noReturn(Class<T> dataClass) {
        return new TypedEvent<>(dataClass);
    }

    public R post(T data) {
        return NativeEventPosting.INSTANCE.postEventWithResult(this, data);
    }
}
