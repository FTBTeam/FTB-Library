package dev.ftb.mods.ftblibrary.util.result;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/// An extended version of ActionResult that also carry a success data of type T when the result is SUCCESS. FAIL and PASS results will not carry any data.
public class HolderActionResult<T> {
    private final @Nullable T successData;
    private final boolean isFail;
    private final boolean isPass;

    private HolderActionResult(@Nullable T successData, boolean isFail, boolean isPass) {
        this.successData = successData;
        this.isFail = isFail;
        this.isPass = isPass;
    }

    public static <T> HolderActionResult<T> success(T data) {
        return new HolderActionResult<>(data, false, false);
    }

    public static <T> HolderActionResult<T> fail() {
        return new HolderActionResult<>(null, true, false);
    }

    public static <T> HolderActionResult<T> pass() {
        return new HolderActionResult<>(null, false, true);
    }

    public boolean isSuccess() {
        return successData != null;
    }

    public boolean isFail() {
        return isFail;
    }

    public boolean isPass() {
        return isPass;
    }

    public void ifSuccess(Consumer<T> consumer) {
        if (successData != null) {
            consumer.accept(successData);
        }
    }

    public void ifFail(Runnable runnable) {
        if (isFail) {
            runnable.run();
        }
    }

    public void ifPass(Runnable runnable) {
        if (isPass) {
            runnable.run();
        }
    }
}
