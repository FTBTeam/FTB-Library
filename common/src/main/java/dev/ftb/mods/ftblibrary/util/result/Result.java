package dev.ftb.mods.ftblibrary.util.result;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/// Result type that can either contain a value of type D or an error of type E.
///
/// @param <D> The type of the data value.
/// @param <E> The type of the error value.
public class Result<D, E> {
    private @Nullable final D data;
    private @Nullable final E error;

    private Result(@Nullable D data, @Nullable E error) {
        this.data = data;
        this.error = error;
    }

    public static <D, E> Result<D, E> success(D data) {
        return new Result<>(data, null);
    }

    public static <D, E> Result<D, E> error(E error) {
        return new Result<>(null, error);
    }

    public boolean isOk() {
        return data != null;
    }

    public boolean isError() {
        return error != null;
    }

    public D unwrap() {
        if (data == null) {
            throw new IllegalStateException("Result does not contain data");
        }

        return data;
    }

    public E unwrapError() {
        if (error == null) {
            throw new IllegalStateException("Result does not contain an error");
        }

        return error;
    }

    public void ifOk(Consumer<D> consumer) {
        if (data != null) {
            consumer.accept(data);
        }
    }

    public void ifError(Consumer<E> consumer) {
        if (error != null) {
            consumer.accept(error);
        }
    }

    public D orThrow() {
        if (!isOk()) {
            throw new IllegalStateException("Result does not contain data");
        }

        assert data != null;
        return data;
    }

    public D orElse(D defaultValue) {
        return data != null ? data : defaultValue;
    }

    public D orElseGet(Supplier<D> supplier) {
        return data != null ? data : supplier.get();
    }
}
