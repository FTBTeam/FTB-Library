package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

public class SyncKnownServerRegistriesPacket extends BaseS2CMessage {
	private final KnownServerRegistries knownServerRegistries;

	public SyncKnownServerRegistriesPacket(FriendlyByteBuf buf) {
		knownServerRegistries = new KnownServerRegistries(buf);
	}

	public SyncKnownServerRegistriesPacket(KnownServerRegistries r) {
		knownServerRegistries = r;
	}

	@Override
	public MessageType getType() {
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
