package dev.ftb.mods.ftblibrary.platform.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ServiceLoader;

public interface PlatformClient {
    PlatformClient INSTANCE = ServiceLoader.load(PlatformClient.class).findFirst().orElseThrow();

    static PlatformClient get() {
        return INSTANCE;
    }

    void sendToServer(CustomPacketPayload payload);
}
