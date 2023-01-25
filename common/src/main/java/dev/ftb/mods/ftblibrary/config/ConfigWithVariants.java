package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.ui.input.MouseButton;

/**
 * A config value with some well-defined options, which can be cycled through.
 *
 * @author LatvianModder
 */
public abstract class ConfigWithVariants<T> extends ConfigValue<T> {
	/**
	 * Get the next (or previous) valid value for this config item.
	 * @param currentValue the current value
	 * @param next true to get the next value, false to get the previous value
	 * @return the next or previous valid value, as appropriate
	 */
	public abstract T getIteration(T currentValue, boolean next);

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		if (value != null && getCanEdit()) {
			setCurrentValue(getIteration(value, button.isLeft()));
			callback.save(true);
		}
	}
}