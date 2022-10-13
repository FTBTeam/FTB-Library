package dev.ftb.mods.ftblibrary.util.fabric;

import net.minecraft.network.chat.Component;

public class TextComponentUtilsImpl {
	public static Component withLinks(String message) {
		return Component.literal(message);
	}
}
