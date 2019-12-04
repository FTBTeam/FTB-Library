package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;

import java.util.Optional;

/**
 * @author LatvianModder
 */
public abstract class ConfigFromString<T> extends ConfigValue<T>
{
	public abstract Optional<T> getValueFromString(String string);

	public String getStringFromValue(T value)
	{
		return value.toString();
	}

	@Override
	public void onClicked(MouseButton button, Runnable callback)
	{
		new GuiEditConfigFromString<>(this, callback).openGui();
	}
}