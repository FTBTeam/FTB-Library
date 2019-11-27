package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;

/**
 * @author LatvianModder
 */
public class SimpleButton extends Button
{
	public interface Callback
	{
		void onClicked(SimpleButton widget, MouseButton button);
	}

	private final Callback consumer;

	public SimpleButton(Panel panel, String text, Icon icon, Callback c)
	{
		super(panel, text, icon);
		consumer = c;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		consumer.onClicked(this, button);
	}
}