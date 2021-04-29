package dev.ftb.mods.ftblibrary.sidebar;

import me.shedaniel.architectury.ForgeEvent;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
@ForgeEvent
public class SidebarButtonCreatedEvent {
	public static final Event<Consumer<SidebarButtonCreatedEvent>> EVENT = EventFactory.createConsumerLoop(SidebarButtonCreatedEvent.class);
	private final SidebarButton button;

	public SidebarButtonCreatedEvent(SidebarButton b) {
		button = b;
	}

	public SidebarButton getButton() {
		return button;
	}
}