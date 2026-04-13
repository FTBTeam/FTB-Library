package dev.ftb.mods.ftblibrary.fabric.platform.networking;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class Client2ServerContext implements PacketContext {
    private final ServerPlayNetworking.Context originalContext;
    private Runnable task;

    public Client2ServerContext(ServerPlayNetworking.Context original) {
        this.originalContext = original;
    }

    @Override
    public Player player() {
        return originalContext.player();
    }

    @Override
    public ConnectionProtocol protocol() {
        return originalContext.player().connection.protocol();
    }

    @Override
    public void reply(Packet<?> packet) {
        originalContext.responseSender().sendPacket(packet);
    }

    @Override
    public void reply(CustomPacketPayload payload) {
        originalContext.responseSender().sendPacket(payload);
    }

    @Override
    public void enqueue(Runnable task) {
        this.task = task;
    }

    @Override
    public Runnable task() {
        return task;
    }
}
