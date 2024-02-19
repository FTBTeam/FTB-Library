package dev.ftb.mods.ftblibrary.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TextComponentUtils {
	@ExpectPlatform
	public static Component withLinks(String message) {
		throw new AssertionError();
	}

	public static Component hotkeyTooltip(String txt) {
		return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
				.append(Component.literal(txt).withStyle(ChatFormatting.GRAY))
				.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
	}
}
