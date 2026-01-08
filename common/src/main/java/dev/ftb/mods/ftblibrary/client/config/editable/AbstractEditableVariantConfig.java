package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;

/**
 * A config value with some well-defined options, which can be cycled through.
 */
public abstract class AbstractEditableVariantConfig<T> extends AbstractEditableConfigValue<T> {
    /**
     * Get the next (or previous) valid value for this config item.
     * @param currentValue the current value
     * @param next true to get the next value, false to get the previous value
     * @return the next or previous valid value, as appropriate
     */
    public abstract T getIteration(T currentValue, boolean next);

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        if (value != null && getCanEdit()) {
            boolean changed = updateValue(getIteration(value, button.isLeft()));
            callback.save(changed);
        }
    }
}