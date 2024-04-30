package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class SyncKnownServerRegistriesPacket extends BaseS2CMessage {
	private final KnownServerRegistries knownServerRegistries;

	public SyncKnownServerRegistriesPacket(RegistryFriendlyByteBuf buf) {
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
	public void write(RegistryFriendlyByteBuf buf) {
		knownServerRegistries.write(buf);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		KnownServerRegistries.client = knownServerRegistries;
	}
}
