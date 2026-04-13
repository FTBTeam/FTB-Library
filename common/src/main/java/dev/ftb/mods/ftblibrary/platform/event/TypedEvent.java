package dev.ftb.mods.ftblibrary.platform.event;

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
