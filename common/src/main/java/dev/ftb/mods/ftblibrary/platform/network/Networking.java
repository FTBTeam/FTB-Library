package dev.ftb.mods.ftblibrary.platform.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface Networking {
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}
