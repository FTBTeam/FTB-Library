package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

public class SyncKnownServerRegistriesPacket extends BaseS2CPacket {
	private final KnownServerRegistries knownServerRegistries;

	public SyncKnownServerRegistriesPacket(FriendlyByteBuf buf) {
		knownServerRegistries = new KnownServerRegistries(buf);
	}

	public SyncKnownServerRegistriesPacket(KnownServerRegistries r) {
		knownServerRegistries = r;
	}

	@Override
	public PacketID getId() {
		return FTBLibraryNet.SYNC_KNOWN_SERVER_REGISTRIES;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		knownServerRegistries.write(buf);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		KnownServerRegistries.client = knownServerRegistries;
	}
}
