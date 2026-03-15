package dev.ftb.mods.ftblibrary.neoforge.platform.networking;

import dev.ftb.mods.ftblibrary.platform.network.NetworkRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

public class NeoNetworkRegistryImpl implements NetworkRegistry {
    private boolean packetsCollected = false;
    private List<PacketHolder<?>> s2cPackets = new ArrayList<>();
    private List<PacketHolder<?>> c2sPackets = new ArrayList<>();

    @Override
    public <T extends CustomPacketPayload> void serverToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PacketHandler<T> handler) {
        if (packetsCollected) {
            throw new IllegalStateException("Cannot register packets after collection!");
        }

        s2cPackets.add(new PacketHolder<>(type, codec, handler));
    }

    @Override
    public <T extends CustomPacketPayload> void clientToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PacketHandler<T> handler) {
        if (packetsCollected) {
            throw new IllegalStateException("Cannot register packets after collection!");
        }

        c2sPackets.add(new PacketHolder<>(type, codec, handler));
    }

    public <T extends CustomPacketPayload> void collectPackets(PayloadRegistrar registrar) {
        packetsCollected = true;

        s2cPackets.forEach(holder -> registerS2C(registrar, holder));
        c2sPackets.forEach(holder -> registerC2S(registrar, holder));

        // Empty the lists, we'll never need them again and this allows the GC to reclaim some memory
        s2cPackets = null;
        c2sPackets = null;
    }

    private <T extends CustomPacketPayload> void registerS2C(PayloadRegistrar registrar, PacketHolder<T> holder) {
        registrar.playToClient(holder.type(), holder.codec(), (payload, context) -> {
            var createdContext = new Server2ClientContext(context);
            holder.handler().handle(payload, createdContext);

            if (createdContext.task() != null) {
                // Run anything that has been enqueued
                context.enqueueWork(createdContext.task());
            }
        });
    }

    private <T extends CustomPacketPayload> void registerC2S(PayloadRegistrar registrar, PacketHolder<T> holder) {
        registrar.playToServer(holder.type(), holder.codec(), (payload, context) -> {
            var createdContext = new Client2ServerContext(context);
            holder.handler().handle(payload, createdContext);

            if (createdContext.task() != null) {
                // Run anything that has been enqueued
                context.enqueueWork(createdContext.task());
            }
        });
    }

    record PacketHolder<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PacketHandler<T> handler) {}
}
