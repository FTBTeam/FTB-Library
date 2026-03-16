package dev.ftb.mods.ftblibrary.platform.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Map;
import java.util.ServiceLoader;

public interface PlatformClient {
    PlatformClient INSTANCE = ServiceLoader.load(PlatformClient.class).findFirst().orElseThrow();

    static PlatformClient get() {
        return INSTANCE;
    }

    void sendToServer(CustomPacketPayload payload);

    void addResourcePackReloadListeners(String modId, Map<Identifier, PreparableReloadListener> listeners);

    default void addResourcePackReloadListener(String modId, Identifier id, PreparableReloadListener listener) {
        addResourcePackReloadListeners(modId, Map.of(id, listener));
    }
}
