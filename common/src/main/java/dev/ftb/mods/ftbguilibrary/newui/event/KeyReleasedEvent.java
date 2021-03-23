package dev.ftb.mods.ftbguilibrary.newui.event;

import dev.ftb.mods.ftbguilibrary.utils.KeyModifiers;

/**
 * @author LatvianModder
 */
public class KeyReleasedEvent {
	public final int keyCode, scanCode;
	public final KeyModifiers modifiers;

	public KeyReleasedEvent(int k, int s, KeyModifiers m) {
		keyCode = k;
		scanCode = s;
		modifiers = m;
	}
}
