package dev.ftb.mods.ftblibrary.util.result;

import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/// An extended version of {@link Outcome} that can hold a failure or success value.
/// Success & failures *can*, but are not required to, hold a value. PASS results should not hold a value.
public class DataOutcome<T> {
    private final @Nullable T data;
    private final Outcome outcome;

    private DataOutcome(@Nullable T data, Outcome outcome) {
        this.data = data;
        this.outcome = outcome;
    }

    public static <T> DataOutcome<T> success(@Nullable T data) {
        return new DataOutcome<>(data, Outcome.SUCCESS);
    }

    public static <T> DataOutcome<T> success() {
        return new DataOutcome<>(null, Outcome.SUCCESS);
    }

    public static <T> DataOutcome<T> fail(T data) {
        return new DataOutcome<>(data, Outcome.FAIL);
    }

    public static <T> DataOutcome<T> fail() {
        return new DataOutcome<>(null, Outcome.FAIL);
    }

    public static <T> DataOutcome<T> pass() {
        return new DataOutcome<>(null, Outcome.PASS);
    }

    public boolean isSuccess() {
        return outcome.isSuccess();
    }

    public boolean isFail() {
        return outcome.isFail();
    }

    public boolean isPass() {
        return outcome.isPass();
    }

    public void ifSuccess(Consumer<@Nullable T> consumer) {
        if (isSuccess()) {
            consumer.accept(data);
        }
    }

    public void ifFail(Consumer<@Nullable T> consumer) {
        if (isFail()) {
            consumer.accept(data);
        }
    }

    public void ifPass(Runnable runnable) {
        if (isPass()) {
            runnable.run();
        }
    }

    /// Gets the data held by this result. Will throw if this is a PASS result, as PASS results should not have data.
    public Optional<T> data() {
        if (isPass()) {
            return Optional.empty();
        }

        return Optional.ofNullable(data);
    }

    @Nullable
    public T dataOrNull() {
        if (isPass()) {
            return null;
        }

        return data;
    }
}
