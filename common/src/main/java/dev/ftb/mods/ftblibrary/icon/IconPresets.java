package dev.ftb.mods.ftblibrary.icon;

import java.util.HashMap;
import java.util.Map;


public class IconPresets {
	public static final Map<String, Icon> MAP = new HashMap<>();

	static {
		MAP.put("#gray_button", IconWithBorder.BUTTON_GRAY);
		MAP.put("#red_button", IconWithBorder.BUTTON_RED);
		MAP.put("#green_button", IconWithBorder.BUTTON_GREEN);
		MAP.put("#blue_button", IconWithBorder.BUTTON_BLUE);
		MAP.put("#gray_round_button", IconWithBorder.BUTTON_ROUND_GRAY);
		MAP.put("#red_round_button", IconWithBorder.BUTTON_ROUND_RED);
		MAP.put("#green_round_button", IconWithBorder.BUTTON_ROUND_GREEN);
		MAP.put("#blue_round_button", IconWithBorder.BUTTON_ROUND_BLUE);
	}
}
