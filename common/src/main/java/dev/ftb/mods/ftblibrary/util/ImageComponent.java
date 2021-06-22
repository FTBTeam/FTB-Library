package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.TextComponent;

public class ImageComponent extends TextComponent {
	public Icon image = Icon.EMPTY;
	public int width = 100;
	public int height = 100;
	public int align = 1;
	public boolean fit = false;

	public ImageComponent() {
		super("[Image]");
	}
}
