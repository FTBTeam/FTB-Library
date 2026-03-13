package dev.ftb.mods.ftblibrary.fabric.platform.networking;

import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.network.NetworkRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FabricNetworkRegistryImpl implements NetworkRegistry {
    @Override
    public <T extends CustomPacketPayload> void serverToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PacketHandler<T> handler) {
        PayloadTypeRegistry.clientboundPlay().register(type, codec);

        // Only register the receiver on the client (I think)
        if (!Platform.get().env().isClient()) {
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                Server2ClientContext createdContext = new Server2ClientContext(context);
                handler.handle(payload, createdContext);

                // TODO: We might not need this.
                if (createdContext.task() != null) {
                    // Run anything that has been enqueued
                    createdContext.task().run();
                }
            });
        }
    }

    @Override
    public <T extends CustomPacketPayload> void clientToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PacketHandler<T> handler) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, ((payload, context) -> {
            Client2ServerContext createdContext = new Client2ServerContext(context);
            handler.handle(payload, createdContext);
            if (createdContext.task() != null) {
                // Run anything that has been enqueued
                createdContext.task().run();
            }
        }));
    }
}
