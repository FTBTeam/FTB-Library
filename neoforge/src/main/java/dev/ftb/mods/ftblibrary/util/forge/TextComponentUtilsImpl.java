package dev.ftb.mods.ftblibrary.util.forge;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.CommonHooks;

public class TextComponentUtilsImpl {
	public static Component withLinks(String message) {
		return CommonHooks.newChatWithLinks(message);
	}
}
