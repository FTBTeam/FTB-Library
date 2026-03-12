package dev.ftb.mods.ftblibrary.platform.event;

public abstract class Event<T> {
    protected volatile T invoker;

    public final T invoker() {
        return invoker;
    }

    public abstract void register(T listener);
}
