package com.feed_the_beast.mods.ftbguilibrary.newui.event;

import com.feed_the_beast.mods.ftbguilibrary.utils.KeyModifiers;

/**
 * @author LatvianModder
 */
public class KeyCharEvent
{
	public final char character;
	public final KeyModifiers modifiers;

	public KeyCharEvent(char c, KeyModifiers m)
	{
		character = c;
		modifiers = m;
	}
}
