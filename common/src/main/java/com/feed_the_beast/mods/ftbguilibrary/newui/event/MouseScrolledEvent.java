package com.feed_the_beast.mods.ftbguilibrary.newui.event;

/**
 * @author LatvianModder
 */
public class MouseScrolledEvent extends MouseEvent
{
	public final double scroll;
	public final boolean up;
	public final double ascroll;

	public MouseScrolledEvent(double _x, double _y, double s)
	{
		super(_x, _y);
		scroll = s;
		up = scroll < 0D;
		ascroll = Math.abs(scroll);
	}
}