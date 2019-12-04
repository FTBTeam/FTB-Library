package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class ConfigWithVariants<T> extends ConfigValue<T>
{
	public abstract T getIteration(T value, boolean next);

	public abstract List<T> getVariants();

	@Override
	public void onClicked(MouseButton button, Runnable callback)
	{
		if (canEdit)
		{
			setCurrentValue(getIteration(current, button.isLeft()));
			callback.run();
		}
	}
}