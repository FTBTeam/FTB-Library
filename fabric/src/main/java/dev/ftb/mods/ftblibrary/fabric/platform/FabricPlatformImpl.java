package dev.ftb.mods.ftblibrary.fabric.platform;

import dev.ftb.mods.ftblibrary.platform.*;
import dev.ftb.mods.ftblibrary.platform.network.NetworkingShim;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Collection;
import java.util.List;

public class FabricPlatformImpl implements Platform {
    private final Paths paths = new FabricPathsImpl();
    private final Misc misc = new FabricMiscImpl();
    private final NetworkingShim networking = new FabricNetworkingImpl();

    @Override
    public Env env() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return Env.CLIENT;
        } else {
            return Env.SERVER;
        }
    }

    @Override
    public boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isFabric() {
        return true;
    }

    @Override
    public boolean isNeoForge() {
        return false; // NEVER CHANGE ME!
    }

    @Override
    public Mod getMod(String modId) {
        return null;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Collection<Mod> getMods() {
        return List.of();
    }

    @Override
    public Misc misc() {
        return misc;
    }

    @Override
    public Paths paths() {
        return paths;
    }

    @Override
    public NetworkingShim networking() {
        return networking;
    }
}
