package dev.ftb.mods.ftblibrary.net;

import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.Env;
import net.minecraft.resources.ResourceLocation;

public class FTBNetworkHandler {
	public static ResourceLocation register(ResourceLocation id, PacketDecoder decoder) {
		NetworkManager.NetworkReceiver receiver = decoder.createReceiver();
		NetworkManager.registerReceiver(NetworkManager.c2s(), id, receiver);

		if (Platform.getEnvironment() == Env.CLIENT) {
			NetworkManager.registerReceiver(NetworkManager.s2c(), id, receiver);
		}

		return id;
	}
}
