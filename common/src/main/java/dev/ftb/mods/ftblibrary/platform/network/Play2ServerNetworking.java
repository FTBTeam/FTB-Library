package dev.ftb.mods.ftblibrary.platform.network;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class Play2ServerNetworking {
    public static void send(CustomPacketPayload payload) {
        PlatformClient.get().sendToServer(payload);
    }
}
