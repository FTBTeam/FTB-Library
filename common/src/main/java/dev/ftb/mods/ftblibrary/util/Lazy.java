package dev.ftb.mods.ftblibrary.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Lazy initialization of a given supplier
 *
 * @param <T> The type of the value
 */
public class Lazy<T> implements Supplier<T> {
    private final Supplier<T> valueSupplier;

    private transient T value;
    private transient volatile boolean initialized;

    private Lazy(Supplier<@NotNull T> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public static <T> Lazy<T> of(Supplier<@NotNull T> valueSupplier) {
        return new Lazy<>(valueSupplier);
    }

    /**
     * Gets the value, computing it if necessary
     */
    @Override
    public T get() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    value = valueSupplier.get();
                    initialized = true;
                }
            }
        }

        return value;
    }

    /**
     * Clears the cached value, causing it to be recomputed on the next call to {@link #get()}
     */
    public synchronized void invalidate() {
        initialized = false;
        value = null;
    }

    @Override
    public String toString() {
        return "Lazy(" + (initialized ? value : "uninitialized") + ")";
    }
}
