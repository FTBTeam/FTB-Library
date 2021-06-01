package dev.ftb.mods.ftblibrary.util;

import net.minecraft.network.chat.Component;

import java.util.Map;

public interface CustomComponentParser {
	Component parse(String string, Map<String, String> properties);
}