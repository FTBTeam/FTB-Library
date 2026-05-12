package dev.ftb.mods.ftblibrary.platform.client;

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

    /// Send a network packet to the server.
    ///
    /// @param payload the packet to send
    void sendToServer(CustomPacketPayload payload);

    /// Register one or more resource pack reload listeners, to be called when client-side resource packs reload.
    ///
    /// @param modId your mod's unique ID
    /// @param listeners a map of unique listener ID's to the listeners to register
    void addResourcePackReloadListeners(String modId, Map<Identifier, PreparableReloadListener> listeners);

    /// Register a resource pack reload listeneers, to be called when client-side resource packs reload.
    ///
    /// @param modId your mod's unique ID
    /// @param id the reload listener's unique ID
    /// @param listener the listener to register
    default void addResourcePackReloadListener(String modId, Identifier id, PreparableReloadListener listener) {
        addResourcePackReloadListeners(modId, Map.of(id, listener));
    }

    /// Register one or more keymappings with vanilla.
    ///
    /// **IMPORTANT**: all keymappings for any single keymapping category _must_ be registered in a single call to this
    /// method, or you will get a "Category is already registered" exception from vanilla. It is fine to call this method
    /// multiple times, as long as the same category isn't passed to more than one invocation of this method.
    ///
    /// @param modId your mod's unique ID
    /// @param keyMappings one or more keymapping objects
    /// @throws IllegalArgumentException if the same category is passed to multiple cals of this method, or if the
    /// method is called with no keymappings
    void registerKeyMapping(String modId, KeyMapping... keyMappings);
}
