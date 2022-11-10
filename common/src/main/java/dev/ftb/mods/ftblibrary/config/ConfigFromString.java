package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public abstract class ConfigFromString<T> extends ConfigValue<T> {
	public abstract boolean parse(@Nullable Consumer<T> callback, String string);

	public String getStringFromValue(@Nullable T v) {
		return String.valueOf(v);
	}

	@Override
	public Component getStringForGUI(@Nullable T v) {
		return Component.literal(getStringFromValue(v));
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new EditConfigFromStringScreen<>(this, callback).openGui();
	}
}
