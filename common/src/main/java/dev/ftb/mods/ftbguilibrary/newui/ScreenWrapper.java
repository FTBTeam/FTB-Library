package dev.ftb.mods.ftbguilibrary.newui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class ScreenWrapper extends Screen {
	public final UI ui;

	public ScreenWrapper(String id, Component title) {
		super(title);
		ui = new UI(id, title);
	}
}
