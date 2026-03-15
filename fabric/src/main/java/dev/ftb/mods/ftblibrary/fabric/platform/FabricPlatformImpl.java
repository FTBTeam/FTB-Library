package dev.ftb.mods.ftblibrary.fabric.platform;

import dev.ftb.mods.ftblibrary.fabric.platform.registry.XRegistryFabric;
import dev.ftb.mods.ftblibrary.fabric.platform.transfer.FabricTransferImpl;
import dev.ftb.mods.ftblibrary.platform.*;
import dev.ftb.mods.ftblibrary.platform.network.Networking;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;
import dev.ftb.mods.ftblibrary.platform.transfer.Transfer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class FabricPlatformImpl implements Platform {
    private final Paths paths = new FabricPathsImpl();
    private final Misc misc = new FabricMiscImpl();
    private final Networking networking = new FabricNetworkingImpl();
    private final Transfer transfer = new FabricTransferImpl();

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
    public Optional<Mod> getMod(String modId) {
        return Optional.ofNullable(FabricLoader.getInstance().getModContainer(modId)
                .map(FabricModImpl::of)
                .orElse(null));
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Collection<Mod> getMods() {
        return FabricLoader.getInstance().getAllMods()
                .stream()
                .map(FabricModImpl::of)
                .collect(Collectors.toCollection(ArrayList::new));
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
    public Networking networking() {
        return networking;
    }

    @Override
    public Transfer transfer() {
        return transfer;
    }

    @Override
    public <T> XRegistry<T> createRegistry(String modId, ResourceKey<Registry<T>> registryKey) {
        return new XRegistryFabric<>(modId, registryKey);
    }
}
