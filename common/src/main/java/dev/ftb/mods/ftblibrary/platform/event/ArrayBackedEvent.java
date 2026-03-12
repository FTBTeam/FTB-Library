package dev.ftb.mods.ftblibrary.platform.event;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.function.Function;

public class ArrayBackedEvent<T> extends Event<T> {
    private final Function<T[], T> invokerFactory;
    private final Object lock = new Object();
    private volatile T[] invokers;

    @SuppressWarnings("unchecked")
    public ArrayBackedEvent(Class<? super T> listenerType, Function<T[], T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.invokers = (T[]) Array.newInstance(listenerType, 0);
        this.invoker = invokerFactory.apply(this.invokers);
    }

    @Override
    public void register(T listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        synchronized (lock) {
            int length = invokers.length;
            T[] newInvokers = (T[]) Array.newInstance(invokers.getClass().getComponentType(), length + 1);
            System.arraycopy(invokers, 0, newInvokers, 0, length);
            newInvokers[length] = listener;
            invokers = newInvokers;
            invoker = invokerFactory.apply(invokers);
        }
    }
}
