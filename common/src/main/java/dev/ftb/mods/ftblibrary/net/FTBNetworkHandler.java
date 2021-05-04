package dev.ftb.mods.ftblibrary.net;

import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.Env;
import net.minecraft.resources.ResourceLocation;

public class FTBNetworkHandler {
	public static FTBNetworkHandler create(String modid) {
		return new FTBNetworkHandler(modid);
	}

	public final String modid;

	private FTBNetworkHandler(String m) {
		modid = m;
	}

	public PacketID register(String id, PacketDecoder decoder) {
		PacketID packetID = new PacketID(this, new ResourceLocation(modid, id));
		NetworkManager.NetworkReceiver receiver = decoder.createReceiver();

		NetworkManager.registerReceiver(NetworkManager.c2s(), packetID.id, receiver);

		if (Platform.getEnvironment() == Env.CLIENT) {
			NetworkManager.registerReceiver(NetworkManager.s2c(), packetID.id, receiver);
		}

		return packetID;
	}
}
