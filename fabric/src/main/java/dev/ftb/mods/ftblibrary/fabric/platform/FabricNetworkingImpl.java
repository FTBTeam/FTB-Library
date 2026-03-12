package dev.ftb.mods.ftblibrary.fabric.platform;

import dev.ftb.mods.ftblibrary.platform.network.NetworkingShim;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkingImpl implements NetworkingShim {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {

    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {

    }
}
