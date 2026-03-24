package dev.ftb.mods.ftblibrary.api.neoforge;

import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.api.event.client.RegisterCustomColorEvent;
import dev.ftb.mods.ftblibrary.api.event.client.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.sidebar.RegisteredSidebarButton;
import net.minecraft.network.chat.TextColor;
import net.neoforged.bus.api.ICancellableEvent;

public class FTBLibraryEvent {
    public static class CustomClick extends BaseEventWithData<CustomClickEvent.Data> implements ICancellableEvent {
        public CustomClick(CustomClickEvent.Data data) {
            super(data);
        }
    }

    public static class RegisterCustomColor extends BaseEventWithData<RegisterCustomColorEvent.Data> {
        public RegisterCustomColor(RegisterCustomColorEvent.Data data) {
            super(data);
        }

        public void addColor(String id, TextColor color) {
            data.addColor(id, color);
        }
    }

    public static class SidebarButtonCreated extends BaseEventWithData<SidebarButtonCreatedEvent.Data> {
        public SidebarButtonCreated(SidebarButtonCreatedEvent.Data data) {
            super(data);
        }

        public RegisteredSidebarButton getButton() {
            return data.button();
        }
    }
}
