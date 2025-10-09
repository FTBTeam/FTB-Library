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
    private T value;

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
        if (value == null) {
            value = valueSupplier.get();
        }

        return value;
    }

    /**
     * Clears the cached value, causing it to be recomputed on the next call to {@link #get()}
     */
    public void clear() {
        value = null;
    }
}
