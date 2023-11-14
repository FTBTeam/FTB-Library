package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.SelectImagePreScreen;


public class ImageConfig extends StringConfig {
	public ImageConfig() {
		super(null);
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new SelectImagePreScreen(this, callback).openGui();
	}
}
