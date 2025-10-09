package dev.ftb.mods.ftblibrary.util.text;

import net.minecraft.network.chat.TextColor;

import java.util.HashMap;
import java.util.Map;

public class ExtendableTextColor {
    private static final Map<String, TextColor> additionalColors = new HashMap<>();

    public static Map<String, TextColor> getCustomColors() {
        return additionalColors;
    }

    public static void addCustomColor(String id, TextColor color) {
        additionalColors.put(id, color);
    }
}
