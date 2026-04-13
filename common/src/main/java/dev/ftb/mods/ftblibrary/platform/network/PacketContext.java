package dev.ftb.mods.ftblibrary.platform.network;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface PacketContext {
    Player player();

    ConnectionProtocol protocol();

    void reply(Packet<?> packet);

    void reply(CustomPacketPayload payload);

    void enqueue(Runnable task);

    Runnable task();
}
