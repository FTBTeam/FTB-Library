package dev.ftb.mods.ftblibrary.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

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

	public static Component translatedDimension(ResourceKey<Level> key) {
		return translatedDimension(key.location());
	}

	public static Component translatedDimension(ResourceLocation dimId) {
		return Component.translatableWithFallback(dimId.toLanguageKey("dimension"), dimId.toString());
	}
}
