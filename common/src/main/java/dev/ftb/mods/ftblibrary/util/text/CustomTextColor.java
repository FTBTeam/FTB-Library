package dev.ftb.mods.ftblibrary.util.text;

import net.minecraft.network.chat.TextColor;

public abstract class CustomTextColor extends TextColor {
    public CustomTextColor(String name) {
        super(0xFFFFFF, name);
    }
}
