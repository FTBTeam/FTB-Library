package dev.ftb.mods.ftblibrary.platform.event;

public record TypedEvent<T, R>(Class<T> dataClass, Class<R> resultClass) {

    public static <T,R> TypedEvent<T,R> of(Class<T> dataClass, Class<R> resultClass) {
        return new TypedEvent<>(dataClass, resultClass);
    }

    public static <T> TypedEvent<T,Boolean> ofBoolean(Class<T> dataClass) {
        return new TypedEvent<>(dataClass, Boolean.class);
    }

    public R post(T data) {
        return NativeEventPosting.INSTANCE.postEventWithResult(this, data);
    }
}
