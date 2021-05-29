package dev.ftb.mods.ftblibrary.util.fabric;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class TextComponentUtilsImpl {
	public static Component withLinks(String message) {
		return new TextComponent(message);
	}
}
