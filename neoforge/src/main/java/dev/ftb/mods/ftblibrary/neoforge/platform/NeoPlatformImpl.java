package dev.ftb.mods.ftblibrary.neoforge.platform;

import dev.ftb.mods.ftblibrary.neoforge.platform.registry.XRegistryNeo;
import dev.ftb.mods.ftblibrary.neoforge.platform.transfer.NeoTransferImpl;
import dev.ftb.mods.ftblibrary.platform.*;
import dev.ftb.mods.ftblibrary.platform.network.Networking;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;
import dev.ftb.mods.ftblibrary.platform.transfer.Transfer;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class NeoPlatformImpl implements Platform {
    private final Paths paths = new NeoPathsImpl();
    private final Networking networking = new NeoNetworkingImpl();
    private final Transfer transfer = new NeoTransferImpl();
    private final Misc misc = new NeoMiscImpl();

    @Override
    public Env env() {
        return switch (FMLEnvironment.getDist()) {
            case CLIENT -> Env.CLIENT;
            case DEDICATED_SERVER -> Env.SERVER;
        };
    }

    @Override
    public boolean isDev() {
        return !FMLEnvironment.isProduction();
    }

    @Override
    public boolean isFabric() {
        return false;
    }

    @Override
    public boolean isNeoForge() {
        return true; // DO NOT CHANGE!
    }

    @Override
    public Optional<Mod> getMod(String modId) {
        return ModList.get().getMods().stream()
                .filter(e -> e.getModId().equals(modId))
                .findFirst()
                .map(NeoModImpl::new);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Collection<Mod> getMods() {
        return ModList.get().getMods().stream().map(NeoModImpl::new)
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
    public void addDataPackReloadListeners(String modId, Map<Identifier, PreparableReloadListener> listeners) {
        var modBus = ModList.get().getModContainerById(modId)
                .map(ModContainer::getEventBus)
                .orElseThrow();
        modBus.addListener(AddServerReloadListenersEvent.class, event -> listeners.forEach(event::addListener));
    }

    @Override
    public <T> XRegistry<T> createRegistry(String modId, ResourceKey<Registry<T>> registryKey) {
        return new XRegistryNeo<>(modId, registryKey);
    }
}
