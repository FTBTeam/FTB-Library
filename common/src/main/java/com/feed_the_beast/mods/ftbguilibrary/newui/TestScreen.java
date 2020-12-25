package com.feed_the_beast.mods.ftbguilibrary.newui;

import net.minecraft.network.chat.TextComponent;

/**
 * @author LatvianModder
 */
public class TestScreen extends ScreenWrapper
{
	public TestScreen()
	{
		super("test", new TextComponent("Test"));

		ui.add("close", new Button());

		ui.addPanel("buttons", panel -> {
			panel.add("test_1", new Button());
			panel.add("test_2", new Button());
			panel.add("test_3", new Button());
		});
		
		/*
		<ui id="test" title="Test">
			<button id="close">Close</button>
			<panel>
				<button id="test_1">Test 1</button>
				<button id="test_2">Test 2</button>
				<button id="test_3">Test 3</button>
			</panel>
		</ui>
		 */
	}
}
