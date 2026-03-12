package dev.ftb.mods.ftblibrary.platform.network;

import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class Play2ServerNetworking {
    public static void send(CustomPacketPayload payload) {
        Platform.get().networking().sendToServer(payload);
    }
}
