package dev.ftb.mods.ftblibrary.platform.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface NetworkRegistry {
    <T extends CustomPacketPayload> void serverToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PacketHandler<T> handler);

    <T extends CustomPacketPayload> void clientToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, PacketHandler<T> handler);

    @FunctionalInterface
    interface PacketHandler<T> {
        void handle(T payload, PacketContext context);
    }
}
