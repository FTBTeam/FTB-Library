package com.feed_the_beast.mods.ftbguilibrary.event;

import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.function.Consumer;

public interface GuiInitEvent
{
	Event<Post> POST = EventFactory.createLoop(Post.class);

	void onInit(Screen gui, List<AbstractWidget> list, Consumer<AbstractWidget> add);

	interface Post extends GuiInitEvent
	{
	}
}
