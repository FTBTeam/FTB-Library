package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditorScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EditNBTPacket(CompoundTag info, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<EditNBTPacket> TYPE = new Type<>(FTBLibrary.rl("edit_nbt"));

    public static StreamCodec<FriendlyByteBuf, EditNBTPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, EditNBTPacket::info,
            ByteBufCodecs.COMPOUND_TAG, EditNBTPacket::tag,
            EditNBTPacket::new
    );

    public static void handle(EditNBTPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> NBTEditorScreen.openEditor(packet.info, packet.tag));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
