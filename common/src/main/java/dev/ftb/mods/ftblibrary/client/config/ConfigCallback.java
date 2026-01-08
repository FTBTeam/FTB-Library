package dev.ftb.mods.ftblibrary.client.config;

@FunctionalInterface
public interface ConfigCallback {
    void save(boolean accepted);
}
