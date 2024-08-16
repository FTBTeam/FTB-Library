package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncKnownServerRegistriesPacket(KnownServerRegistries registries) implements CustomPacketPayload {
    public static final Type<SyncKnownServerRegistriesPacket> TYPE = new Type<>(FTBLibrary.rl("sync_known_server_registries"));

    public static StreamCodec<RegistryFriendlyByteBuf, SyncKnownServerRegistriesPacket> STREAM_CODEC = StreamCodec.composite(
            KnownServerRegistries.STREAM_CODEC, SyncKnownServerRegistriesPacket::registries,
            SyncKnownServerRegistriesPacket::new
    );

    public static void handle(SyncKnownServerRegistriesPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> KnownServerRegistries.client = packet.registries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
