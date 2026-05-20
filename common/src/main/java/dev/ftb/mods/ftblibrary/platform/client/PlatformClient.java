package dev.ftb.mods.ftblibrary.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.input.Input;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Map;
import java.util.ServiceLoader;

/// This interface abstracts some common clientside actions which have differing NeoForge and Fabric implementations.
public interface PlatformClient {
    PlatformClient INSTANCE = ServiceLoader.load(PlatformClient.class).findFirst().orElseThrow();

    static PlatformClient get() {
        return INSTANCE;
    }

    Input input();

    /// Send a network packet to the server.
    ///
    /// @param payload the packet to send
    void sendToServer(CustomPacketPayload payload);

    /// Register one or more resource pack reload listeners, to be called when client-side resource packs reload.
    ///
    /// @param modId your mod's unique ID
    /// @param listeners a map of unique listener ID's to the listeners to register
    void addResourcePackReloadListeners(String modId, Map<Identifier, PreparableReloadListener> listeners);

    /// Register a resource pack reload listener, to be called when client-side resource packs reload.
    ///
    /// @param modId your mod's unique ID
    /// @param id the reload listener's unique ID
    /// @param listener the listener to register
    default void addResourcePackReloadListener(String modId, Identifier id, PreparableReloadListener listener) {
        addResourcePackReloadListeners(modId, Map.of(id, listener));
    }

    /**
     * @param modId the mod
     * @param keyMappings the mappings
     * @deprecated use {@link Input#registerKeyMapping(String, KeyMapping...)}
     */
    @Deprecated(forRemoval = true)
    default void registerKeyMapping(String modId, KeyMapping... keyMappings) {
        input().registerKeyMapping(modId, keyMappings);
    }
}
