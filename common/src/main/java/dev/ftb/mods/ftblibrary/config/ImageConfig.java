package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.SelectImagePreScreen;

/**
 * @deprecated use {@link ImageResourceConfig}
 */
@Deprecated(forRemoval = true)
public class ImageConfig extends StringConfig {
	public ImageConfig() {
		super(null);
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new SelectImagePreScreen(this, callback).openGui();
	}

	@Override
	public void onClicked(Widget clicked, MouseButton button, ConfigCallback callback) {
		new SelectImagePreScreen(this, callback).openGui();
	}
}
