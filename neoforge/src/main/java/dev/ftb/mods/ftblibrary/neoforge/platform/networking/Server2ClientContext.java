package dev.ftb.mods.ftblibrary.neoforge.platform.networking;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Server2ClientContext implements PacketContext {
    private final IPayloadContext originalContext;
    private Runnable task;

    public Server2ClientContext(IPayloadContext originalContext) {
        this.originalContext = originalContext;
    }

    @Override
    public Player player() {
        return originalContext.player();
    }

    @Override
    public ConnectionProtocol protocol() {
        return originalContext.listener().protocol();
    }

    @Override
    public void reply(Packet<?> packet) {
        originalContext.connection().send(packet);
    }

    @Override
    public void reply(CustomPacketPayload payload) {
        originalContext.reply(payload);
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
