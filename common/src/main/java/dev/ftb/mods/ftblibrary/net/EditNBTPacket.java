package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditorScreen;
import dev.ftb.mods.ftblibrary.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class EditNBTPacket extends BaseS2CMessage {
	private final CompoundTag info;
	private final CompoundTag tag;

	public EditNBTPacket(FriendlyByteBuf buf) {
		info = buf.readNbt();
		tag = buf.readNbt();
	}

	public EditNBTPacket(CompoundTag i, CompoundTag t) {
		info = i;
		tag = t;
	}

	@Override
	public MessageType getType() {
		return FTBLibraryNet.EDIT_NBT;
	}

	@Override
	public void write(RegistryFriendlyByteBuf buf) {
		buf.writeNbt(info);
		buf.writeNbt(tag);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		new NBTEditorScreen(info, tag, (accepted, tag) -> {
			if (accepted) {
				if (NBTUtils.getSizeInBytes(tag, false) >= 30000L) {
					FTBLibrary.LOGGER.error("NBT too large to send!");
				} else {
					new EditNBTResponsePacket(info, tag).sendToServer();
				}
			}
		}).openGui();
	}
}
