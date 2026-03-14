package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditResponseHandlers;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record EditNBTResponsePacket(CompoundTag info, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<EditNBTResponsePacket> TYPE = new Type<>(FTBLibrary.rl("edit_nbt_response"));

    public static StreamCodec<FriendlyByteBuf, EditNBTResponsePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, EditNBTResponsePacket::info,
            ByteBufCodecs.COMPOUND_TAG, EditNBTResponsePacket::tag,
            EditNBTResponsePacket::new
    );

    public static void handle(EditNBTResponsePacket packet, PacketContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            context.enqueue(() -> NBTEditResponseHandlers.INSTANCE.handleResponse(packet.info.getStringOr("type", ""), serverPlayer, packet.info, packet.tag));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
