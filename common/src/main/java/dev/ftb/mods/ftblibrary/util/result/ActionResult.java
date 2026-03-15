package dev.ftb.mods.ftblibrary.util.result;

/// InteractionResult inspired result type that can either be SUCCESS, FAIL, or PASS.
public enum ActionResult {
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

    public <T> T map(T successValue, T failValue, T passValue) {
        return switch (this) {
            case SUCCESS -> successValue;
            case FAIL -> failValue;
            case PASS -> passValue;
        };
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
