package com.feed_the_beast.mods.ftbguilibrary.newui;

import net.minecraft.util.text.StringTextComponent;

/**
 * @author LatvianModder
 */
public class TestScreen extends ScreenWrapper
{
	public TestScreen()
	{
		super("test", new StringTextComponent("Test"));

		ui.add("close", new Button());

		ui.addPanel("buttons", panel -> {
			panel.add("test_1", new Button());
			panel.add("test_2", new Button());
			panel.add("test_3", new Button());
		});
	}
}
