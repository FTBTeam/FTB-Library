package dev.ftb.mods.ftbguilibrary.newui.event;

import dev.ftb.mods.ftbguilibrary.utils.KeyModifiers;

/**
 * @author LatvianModder
 */
public class KeyCharEvent {
	public final char character;
	public final KeyModifiers modifiers;

	public KeyCharEvent(char c, KeyModifiers m) {
		character = c;
		modifiers = m;
	}
}
