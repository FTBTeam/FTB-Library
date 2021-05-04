package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.FTBLibrary;

public interface FTBLibraryNet {
	FTBNetworkHandler NET = FTBNetworkHandler.create(FTBLibrary.MOD_ID);

	PacketID EDIT_NBT = NET.registerS2C("edit_nbt", EditNBTPacket::new);
	PacketID EDIT_NBT_RESPONSE = NET.registerC2S("edit_nbt_response", EditNBTResponsePacket::new);

	static void init() {
	}
}
