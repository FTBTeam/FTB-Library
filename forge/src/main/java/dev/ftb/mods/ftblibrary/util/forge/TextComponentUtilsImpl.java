package dev.ftb.mods.ftblibrary.util.forge;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeHooks;

public class TextComponentUtilsImpl {
    public static Component withLinks(String message) {
        return ForgeHooks.newChatWithLinks(message);
    }
}
