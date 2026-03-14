package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class TextComponentUtils {
    public static Component withLinks(String message) {
        return Platform.get().misc().componentWithLinks(message);
    }

    public static Component hotkeyTooltip(String txt) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(txt).withStyle(ChatFormatting.GRAY))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static Component translatedDimension(ResourceKey<Level> key) {
        return translatedDimension(key.identifier());
    }

    public static Component translatedDimension(Identifier dimId) {
        return Component.translatableWithFallback(dimId.toLanguageKey("dimension"), dimId.toString());
    }
}
