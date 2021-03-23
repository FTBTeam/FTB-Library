package dev.ftb.mods.ftbguilibrary.config;

import dev.ftb.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
		return new TextComponent(getStringFromValue(v));
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new GuiEditConfigFromString<>(this, callback).openGui();
	}
}