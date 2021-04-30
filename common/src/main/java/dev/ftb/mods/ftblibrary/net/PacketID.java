package dev.ftb.mods.ftblibrary.net;

import net.minecraft.resources.ResourceLocation;

public final class PacketID {
	public final FTBNetworkHandler networkHandler;
	public final ResourceLocation id;

	PacketID(FTBNetworkHandler h, ResourceLocation i) {
		networkHandler = h;
		id = i;
	}
}
