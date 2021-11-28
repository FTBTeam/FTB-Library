package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.FTBLibraryCommands;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class EditNBTResponsePacket extends BaseC2SMessage {
	private final CompoundTag info;
	private final CompoundTag tag;

	public EditNBTResponsePacket(FriendlyByteBuf buf) {
		info = buf.readNbt();
		tag = buf.readAnySizeNbt();
	}

	public EditNBTResponsePacket(CompoundTag i, CompoundTag t) {
		info = i;
		tag = t;
	}

	@Override
	public MessageType getType() {
		return FTBLibraryNet.EDIT_NBT_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeNbt(info);
		buf.writeNbt(tag);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();

		if (info.equals(FTBLibraryCommands.EDITING_NBT.remove(player.getUUID()))) {
			switch (info.getString("type")) {
				case "block": {
					BlockPos pos = new BlockPos(info.getInt("x"), info.getInt("y"), info.getInt("z"));

					if (player.level.isLoaded(pos)) {
						BlockEntity blockEntity = player.level.getBlockEntity(pos);

						if (blockEntity != null) {
							tag.putInt("x", pos.getX());
							tag.putInt("y", pos.getY());
							tag.putInt("z", pos.getZ());
							tag.putString("id", info.getString("id"));
							blockEntity.load(tag);
							blockEntity.setChanged();
							player.level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
						}
					}

					break;
				}
				case "entity": {
					Entity entity = player.level.getEntity(info.getInt("id"));

					if (entity != null) {
						UUID uUID = entity.getUUID();
						entity.load(tag);
						entity.setUUID(uUID);
					}

					break;
				}
				case "player": {
					ServerPlayer player1 = player.level.getServer().getPlayerList().getPlayer(info.getUUID("id"));

					if (player1 != null) {
						UUID uUID = player1.getUUID();
						player1.load(tag);
						player1.setUUID(uUID);
						player1.moveTo(player1.getX(), player1.getY(), player1.getZ());
					}

					break;
				}
				case "item": {
					player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.of(tag));
					break;
				}
			}
		}
	}
}
