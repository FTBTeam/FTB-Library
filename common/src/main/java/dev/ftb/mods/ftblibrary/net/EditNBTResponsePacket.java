package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryCommands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.TagValueInput;
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
                                blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, blockEntity.getLevel().registryAccess(), tag));
                                blockEntity.setChanged();
                                player.level().sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    }
                    case "entity" -> {
                        var entity = player.level().getEntity(info.getInt("id").orElseThrow());

                        if (entity != null) {
                            var uuid = entity.getUUID();
                            entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), tag));
                            entity.setUUID(uuid);
                        }
                    }
                    case "player" -> {
                        var targetPlayer = player.getServer().getPlayerList().getPlayer(info.read("id", UUIDUtil.CODEC).orElse(null));

                        if (targetPlayer != null) {
                            var uuid = targetPlayer.getUUID();
                            targetPlayer.load(TagValueInput.create(ProblemReporter.DISCARDING, targetPlayer.registryAccess(), tag));
                            targetPlayer.setUUID(uuid);
                            targetPlayer.setPos(new Vec3(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ()));
                        }
                    }
                    case "item" -> ItemStack.CODEC.parse(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag)
                            .ifSuccess(stack -> player.setItemInHand(InteractionHand.MAIN_HAND, stack));
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
