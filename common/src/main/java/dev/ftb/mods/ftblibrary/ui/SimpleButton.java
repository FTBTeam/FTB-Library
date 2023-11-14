package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;


public class SimpleButton extends Button {
	public interface Callback {
		void onClicked(SimpleButton widget, MouseButton button);
	}

	private final Callback consumer;

	public SimpleButton(Panel panel, Component text, Icon icon, Callback c) {
		super(panel, text, icon);
		consumer = c;
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		consumer.onClicked(this, button);
	}
}
