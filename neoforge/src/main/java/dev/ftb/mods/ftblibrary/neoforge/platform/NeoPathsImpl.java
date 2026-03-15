package dev.ftb.mods.ftblibrary.neoforge.platform;

import dev.ftb.mods.ftblibrary.platform.Paths;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoPathsImpl implements Paths {
    @Override
    public Path gamePath() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public Path configPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path modsPath() {
        return FMLPaths.MODSDIR.get();
    }
}
