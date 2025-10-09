package dev.ftb.mods.ftblibrary.util.text;

import net.minecraft.network.chat.TextColor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtendableTextColor {
    private static final Map<String, TextColor> additionalColors = new ConcurrentHashMap<>();

    public static Map<String, TextColor> getCustomColors() {
        return additionalColors;
    }

    public static void addCustomColor(String id, TextColor color) {
        additionalColors.put(id, color);
    }
}
