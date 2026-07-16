package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class TextComponentUtils {

    private static final Map<String, ChatFormatting> BY_NAME = Arrays.stream(ChatFormatting.values())
            .collect(Collectors.toMap(f -> f.name().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", ""), f -> f));

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

    @Nullable
    public static ChatFormatting getByName(@Nullable String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", ""));
    }
}
