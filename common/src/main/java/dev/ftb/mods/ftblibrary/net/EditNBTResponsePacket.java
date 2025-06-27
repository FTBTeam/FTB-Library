package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryCommands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public record EditNBTResponsePacket(CompoundTag info, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<EditNBTResponsePacket> TYPE = new Type<>(FTBLibrary.rl("edit_nbt_response"));

    public static StreamCodec<FriendlyByteBuf, EditNBTResponsePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, EditNBTResponsePacket::info,
            ByteBufCodecs.COMPOUND_TAG, EditNBTResponsePacket::tag,
            EditNBTResponsePacket::new
    );

    public static void handle(EditNBTResponsePacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var player = (ServerPlayer) context.getPlayer();

            CompoundTag info = packet.info;
            CompoundTag tag = packet.tag;

            if (info.equals(FTBLibraryCommands.EDITING_NBT.remove(player.getUUID()))) {
                switch (info.getStringOr("type", "")) {
                    case "block" -> {
                        var pos = new BlockPos(info.getIntOr("x", 0), info.getIntOr("y", 0), info.getIntOr("z", 0));
                        if (player.level().isLoaded(pos)) {
                            var blockEntity = player.level().getBlockEntity(pos);

                            if (blockEntity != null) {
                                tag.putInt("x", pos.getX());
                                tag.putInt("y", pos.getY());
                                tag.putInt("z", pos.getZ());
                                tag.putString("id", info.getString("id").orElseThrow());
                                // TODO: [1.21.6] Add back
//                                blockEntity.loadWithComponents(tag, player.level().registryAccess());
//                                blockEntity.setChanged();
                                player.level().sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                            }
                        }
                    }
                    case "entity" -> {
                        var entity = player.level().getEntity(info.getInt("id").orElseThrow());

                        if (entity != null) {
                            var uUID = entity.getUUID();
                            // TODO: [1.21.6] Add back
//                            entity.load(tag);
                            entity.setUUID(uUID);
                        }
                    }
                    case "player" -> {
                        var player1 = player.getServer().getPlayerList().getPlayer(info.read("id", UUIDUtil.CODEC).orElse(null));

                        if (player1 != null) {
                            var uUID = player1.getUUID();
                            // TODO: [1.21.6] Add back
//                            player1.load(tag);
                            player1.setUUID(uUID);
                            player1.move(MoverType.PLAYER, new Vec3(player1.getX(), player1.getY(), player1.getZ()));
                        }
                    }
                    // TODO: [1.21.6] Add back
//                    case "item" -> ItemStack.parse(player.registryAccess(), tag)
//                            .ifPresent(stack -> player.setItemInHand(InteractionHand.MAIN_HAND, stack));
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
