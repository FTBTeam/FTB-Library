package com.feed_the_beast.mods.ftbguilibrary.widget;

/**
 * @author LatvianModder
 */
public class WidgetVerticalSpace extends Widget
{
	public WidgetVerticalSpace(Panel p, int h)
	{
		super(p);
		setSize(1, h);
	}

	@Override
	public boolean isEnabled()
	{
		return false;
	}

	@Override
	public boolean shouldDraw()
	{
		return false;
	}
}