package dev.ftb.mods.ftblibrary.util.result;

import java.util.function.Supplier;

/// InteractionResult inspired result type that can either be SUCCESS, FAIL, or PASS.
public enum Outcome {
    SUCCESS,
    FAIL,
    PASS;

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFail() {
        return this == FAIL;
    }

    public boolean isPass() {
        return this == PASS;
    }

    /// Maps the outcome to a value based on the type of outcome it is.
    public <T> T map(Supplier<T> successValue, Supplier<T> failValue, Supplier<T> passValue) {
        return switch (this) {
            case SUCCESS -> successValue.get();
            case FAIL -> failValue.get();
            case PASS -> passValue.get();
        };
    }

    /// Runs the appropriate runnable based on the type of outcome.
    public void run(Runnable onSuccess, Runnable onFail, Runnable onPass) {
        switch (this) {
            case SUCCESS -> onSuccess.run();
            case FAIL -> onFail.run();
            case PASS -> onPass.run();
        }
    }

    public void ifSuccess(Runnable runnable) {
        if (isSuccess()) {
            runnable.run();
        }
    }

    public void ifFail(Runnable runnable) {
        if (isFail()) {
            runnable.run();
        }
    }

    public void ifPass(Runnable runnable) {
        if (isPass()) {
            runnable.run();
        }
    }
}
