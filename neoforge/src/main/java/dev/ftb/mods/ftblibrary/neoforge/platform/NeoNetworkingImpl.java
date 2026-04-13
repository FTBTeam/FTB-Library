package dev.ftb.mods.ftblibrary.neoforge.platform;

import dev.ftb.mods.ftblibrary.neoforge.platform.networking.NeoNetworkRegistryImpl;
import dev.ftb.mods.ftblibrary.platform.network.NetworkRegistry;
import dev.ftb.mods.ftblibrary.platform.network.Networking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoNetworkingImpl implements Networking {
    private final NetworkRegistry registry = new NeoNetworkRegistryImpl();

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public NetworkRegistry registry() {
        return registry;
    }
}
