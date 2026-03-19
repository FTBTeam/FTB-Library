package dev.ftb.mods.ftblibrary.api.neoforge;

import net.neoforged.bus.api.Event;

public abstract class BaseEventWithData<T> extends Event {
    protected final T data;

    public BaseEventWithData(T data) {
        this.data = data;
    }

    public T getEventData() {
        return data;
    }
}
