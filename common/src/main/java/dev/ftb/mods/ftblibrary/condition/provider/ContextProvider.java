package dev.ftb.mods.ftblibrary.condition.provider;

public abstract class ContextProvider {
    private final String name;

    public ContextProvider(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }
}
