package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public abstract class ConfigFromString<T> extends ConfigValue<T>
{
	public abstract boolean parse(@Nullable Consumer<T> callback, String string);

	public String getStringFromValue(@Nullable T v)
	{
		return String.valueOf(v);
	}

	@Override
	public ITextComponent getStringForGUI(@Nullable T v)
	{
		return new StringTextComponent(getStringFromValue(v));
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback)
	{
		new GuiEditConfigFromString<>(this, callback).openGui();
	}
}