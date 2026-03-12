package dev.ftb.mods.ftblibrary.platform;

import java.util.function.Supplier;

public enum Env {
    CLIENT,
    SERVER;

    public boolean isClient() {
        return this == CLIENT;
    }

    public boolean isServer() {
        return this == SERVER;
    }

    public static void runInEnv(Env env, Supplier<Runnable> task) {
        if (Platform.get().env() == env) {
            task.get().run();
        }
    }
}
