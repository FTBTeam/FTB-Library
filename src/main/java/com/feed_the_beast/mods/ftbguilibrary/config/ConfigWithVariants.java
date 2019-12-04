package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;

/**
 * @author LatvianModder
 */
public abstract class ConfigWithVariants<T> extends ConfigValue<T>
{
	public abstract T getIteration(T value, boolean next);

	@Override
	public void onClicked(MouseButton button, Runnable callback)
	{
		if (getCanEdit())
		{
			setCurrentValue(getIteration(current, button.isLeft()));
			callback.run();
		}
	}
}