package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.nbtedit.NBTEditorScreen;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class EditNBTPacket extends BaseS2CMessage {
	private final CompoundTag info;
	private final CompoundTag tag;

	public EditNBTPacket(FriendlyByteBuf buf) {
		info = buf.readNbt();
		tag = buf.readAnySizeNbt();
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
	public void write(FriendlyByteBuf buf) {
		buf.writeNbt(info);
		buf.writeNbt(tag);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		new NBTEditorScreen(info, tag).openGui();
	}
}
