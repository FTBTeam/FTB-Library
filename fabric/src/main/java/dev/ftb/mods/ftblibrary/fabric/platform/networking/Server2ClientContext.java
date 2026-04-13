package dev.ftb.mods.ftblibrary.fabric.platform.networking;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class Server2ClientContext implements PacketContext {
    private final ClientPlayNetworking.Context originalContext;
    private Runnable task;

    public Server2ClientContext(ClientPlayNetworking.Context originalContext) {
        this.originalContext = originalContext;
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
