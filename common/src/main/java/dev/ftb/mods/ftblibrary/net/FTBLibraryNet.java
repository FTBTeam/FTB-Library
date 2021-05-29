package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftblibrary.net.snm.SimpleNetworkManager;

public interface FTBLibraryNet {
	SimpleNetworkManager NET = SimpleNetworkManager.create(FTBLibrary.MOD_ID);

	PacketID EDIT_NBT = NET.registerS2C("edit_nbt", EditNBTPacket::new);
	PacketID EDIT_NBT_RESPONSE = NET.registerC2S("edit_nbt_response", EditNBTResponsePacket::new);
	PacketID SYNC_KNOWN_SERVER_REGISTRIES = NET.registerS2C("sync_known_server_registries", SyncKnownServerRegistriesPacket::new);

	static void init() {
	}
}
