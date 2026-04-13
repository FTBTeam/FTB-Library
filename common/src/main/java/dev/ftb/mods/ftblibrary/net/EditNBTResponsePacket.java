package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditResponseHandlers;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import net.minecraft.IdentifierException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public record EditNBTResponsePacket(CompoundTag info, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<EditNBTResponsePacket> TYPE = new Type<>(FTBLibrary.id("edit_nbt_response"));

    public static StreamCodec<FriendlyByteBuf, EditNBTResponsePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, EditNBTResponsePacket::info,
            ByteBufCodecs.COMPOUND_TAG, EditNBTResponsePacket::tag,
            EditNBTResponsePacket::new
    );

    public static void handle(EditNBTResponsePacket packet, PacketContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer && serverPlayer.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            packet.info.getString("type").ifPresent(idStr -> {
                try {
                    context.enqueue(() -> NBTEditResponseHandlers.INSTANCE.handleResponse(Identifier.parse(idStr), serverPlayer, packet.info, packet.tag));
                } catch (IdentifierException ignored) {
                }
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
