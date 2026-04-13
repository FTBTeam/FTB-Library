package dev.ftb.mods.ftblibrary.fabric.platform;

import dev.ftb.mods.ftblibrary.platform.Paths;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPathsImpl implements Paths {
    @Override
    public Path gamePath() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path configPath() {
        return FabricLoader.getInstance().getGameDir().resolve("config");
    }

    @Override
    public Path modsPath() {
        return FabricLoader.getInstance().getGameDir();
    }
}
