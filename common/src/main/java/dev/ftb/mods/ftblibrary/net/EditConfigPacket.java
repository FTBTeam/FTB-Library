package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EditConfigPacket(boolean isClientConfig) implements CustomPacketPayload {
    public static final Type<EditConfigPacket> TYPE = new Type<>(FTBLibrary.rl("edit_config"));

    public static final StreamCodec<FriendlyByteBuf,EditConfigPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EditConfigPacket::isClientConfig,
            EditConfigPacket::new
    );

    public static void handle(EditConfigPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> FTBLibraryClient.editConfig(packet.isClientConfig));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
