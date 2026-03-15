package dev.ftb.mods.ftblibrary.util.result;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/// An extended version of ActionResult that can hold a failure or success value.
/// Success & failures *can*, but are not required to, hold a value. PASS results should not hold a value.
public class HolderActionResult<T> {
    private final @Nullable T data;
    private final boolean isFail;
    private final boolean isPass;

    private HolderActionResult(@Nullable T data, boolean isFail, boolean isPass) {
        this.data = data;
        this.isFail = isFail;
        this.isPass = isPass;
    }

    public static <T> HolderActionResult<T> success(@Nullable T data) {
        return new HolderActionResult<>(data, false, false);
    }

    public static <T> HolderActionResult<T> success() {
        return new HolderActionResult<>(null, false, false);
    }

    public static <T> HolderActionResult<T> fail(T data) {
        return new HolderActionResult<>(data, true, false);
    }

    public static <T> HolderActionResult<T> fail() {
        return new HolderActionResult<>(null, true, false);
    }

    public static <T> HolderActionResult<T> pass() {
        return new HolderActionResult<>(null, false, true);
    }

    public boolean isSuccess() {
        return !isFail && !isPass;
    }

    public boolean isFail() {
        return isFail;
    }

    public boolean isPass() {
        return isPass;
    }

    public void ifSuccess(Consumer<@Nullable T> consumer) {
        if (data != null) {
            consumer.accept(data);
        }
    }

    public void ifFail(Consumer<@Nullable T> consumer) {
        if (isFail) {
            consumer.accept(data);
        }
    }

    public void ifPass(Runnable runnable) {
        if (isPass) {
            runnable.run();
        }
    }

    /// Gets the data held by this result. Will throw if this is a PASS result, as PASS results should not have data.
    public T data() {
        if (isPass) {
            throw new IllegalStateException("Cannot get data from a PASS result");
        }

        return data;
    }
}
