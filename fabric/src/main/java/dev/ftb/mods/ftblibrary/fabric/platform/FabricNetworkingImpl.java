package dev.ftb.mods.ftblibrary.fabric.platform;

import dev.ftb.mods.ftblibrary.fabric.platform.networking.FabricNetworkRegistryImpl;
import dev.ftb.mods.ftblibrary.platform.network.NetworkRegistry;
import dev.ftb.mods.ftblibrary.platform.network.Networking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkingImpl implements Networking {
    public final FabricNetworkRegistryImpl registry = new FabricNetworkRegistryImpl();

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (ServerPlayNetworking.canSend(player, payload.type())) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public NetworkRegistry registry() {
        return registry;
    }
}
