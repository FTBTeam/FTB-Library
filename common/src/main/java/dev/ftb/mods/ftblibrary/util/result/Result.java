package dev.ftb.mods.ftblibrary.util.result;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/// Result type that can either contain a value of type D or an error of type E.
///
/// @param <D> The type of the data value.
/// @param <E> The type of the error value.
public final class Result<D, E> {
    /// Internal sealed type to track state without using null as a discriminant.
    private sealed interface State<D, E> {
        record Ok<D, E>(D value) implements State<D, E> {}
        record Err<D, E>(E error) implements State<D, E> {}
    }

    private final State<D, E> state;

    private Result(State<D, E> state) {
        this.state = state;
    }

    /// Creates a successful result containing {@code value}. The value may be {@code null}.
    public static <D, E> Result<D, E> ok(D value) {
        return new Result<>(new State.Ok<>(value));
    }

    /// Creates a failed result containing {@code error}. The error may be {@code null}.
    public static <D, E> Result<D, E> err(E error) {
        return new Result<>(new State.Err<>(error));
    }

    /// Returns {@code true} if this result is a success.
    public boolean isOk() {
        return state instanceof State.Ok<D, E>;
    }

    /// Returns {@code true} if this result is an error.
    public boolean isError() {
        return state instanceof State.Err<D, E>;
    }

    /// Returns the success value, or throws {@link IllegalStateException} if this is an error.
    public D unwrap() {
        return switch (state) {
            case State.Ok<D, E>(var value) -> value;
            case State.Err<D, E> e -> throw new IllegalStateException("Called unwrap() on an error result: " + e.error());
        };
    }

    /// Returns the error value, or throws {@link IllegalStateException} if this is a success.
    public E unwrapError() {
        return switch (state) {
            case State.Ok<D, E> ok -> throw new IllegalStateException("Called unwrapError() on an ok result: " + ok.value());
            case State.Err<D, E>(var error) -> error;
        };
    }

    /// Returns the success value, or {@code defaultValue} if this is an error.
    public D unwrapOr(D defaultValue) {
        return switch (state) {
            case State.Ok<D, E>(var value) -> value;
            case State.Err<D, E> __ -> defaultValue;
        };
    }

    /// Returns the success value, or the value supplied by {@code supplier} if this is an error.
    public D unwrapOrElse(Supplier<D> supplier) {
        return switch (state) {
            case State.Ok<D, E>(var value) -> value;
            case State.Err<D, E> __ -> supplier.get();
        };
    }

    /// Returns the success value as an {@link Optional}, or {@link Optional#empty()} if this is an error.
    /// Note: if the success value itself is {@code null}, this will return {@link Optional#empty()}.
    public Optional<D> ok() {
        return switch (state) {
            case State.Ok<D, E>(var value) -> Optional.ofNullable(value);
            case State.Err<D, E> _ -> Optional.empty();
        };
    }

    /// Returns the error value as an {@link Optional}, or {@link Optional#empty()} if this is a success.
    /// Note: if the error value itself is {@code null}, this will return {@link Optional#empty()}.
    public Optional<E> error() {
        return switch (state) {
            case State.Ok<D, E> _ -> Optional.empty();
            case State.Err<D, E>(var error) -> Optional.ofNullable(error);
        };
    }

    /// If this is a success, applies {@code mapper} to the value and returns a new success result.
    /// If this is an error, returns the error unchanged.
    public <U> Result<U, E> map(Function<D, U> mapper) {
        return switch (state) {
            case State.Ok<D, E>(var value) -> Result.ok(mapper.apply(value));
            case State.Err<D, E>(var error) -> Result.err(error);
        };
    }

    /// If this is an error, applies {@code mapper} to the error and returns a new error result.
    /// If this is a success, returns the success unchanged.
    public <F> Result<D, F> mapError(Function<E, F> mapper) {
        return switch (state) {
            case State.Ok<D, E>(var value) -> Result.ok(value);
            case State.Err<D, E>(var error) -> Result.err(mapper.apply(error));
        };
    }

    /// If this is a success, applies {@code mapper} to the value and returns the resulting {@link Result}.
    /// Useful for chaining fallible operations. If this is an error, returns the error unchanged.
    public <U> Result<U, E> flatMap(Function<D, Result<U, E>> mapper) {
        return switch (state) {
            case State.Ok<D, E>(var value) -> mapper.apply(value);
            case State.Err<D, E>(var error) -> Result.err(error);
        };
    }

    /// If this is a success, passes the value to {@code consumer}.
    public void ifOk(Consumer<D> consumer) {
        if (state instanceof State.Ok<D, E>(var value)) {
            consumer.accept(value);
        }
    }

    /// If this is an error, passes the error to {@code consumer}.
    public void ifError(Consumer<E> consumer) {
        if (state instanceof State.Err<D, E>(var error)) {
            consumer.accept(error);
        }
    }

    /// Calls either {@code onOk} or {@code onError} depending on the state of this result.
    public void match(Consumer<D> onOk, Consumer<E> onError) {
        switch (state) {
            case State.Ok<D, E>(var value) -> onOk.accept(value);
            case State.Err<D, E>(var error) -> onError.accept(error);
        }
    }

    /// Maps this result to a value of type {@code U} by applying either
    /// {@code onOk} or {@code onError} depending on the state.
    public <U> U fold(Function<D, U> onOk, Function<E, U> onError) {
        return switch (state) {
            case State.Ok<D, E>(var value) -> onOk.apply(value);
            case State.Err<D, E>(var error) -> onError.apply(error);
        };
    }

    @Override
    public String toString() {
        return switch (state) {
            case State.Ok<D, E>(var value) -> "Result.ok(%s)".formatted(value);
            case State.Err<D, E>(var error) -> "Result.err(%s)".formatted(error);
        };
    }
}
