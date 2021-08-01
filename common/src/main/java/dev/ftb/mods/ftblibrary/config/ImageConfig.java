package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.SelectImageScreen;

/**
 * @author LatvianModder
 */
public class ImageConfig extends StringConfig {
	public ImageConfig() {
		super(null);
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new SelectImageScreen(this, callback).openGui();
	}
}
