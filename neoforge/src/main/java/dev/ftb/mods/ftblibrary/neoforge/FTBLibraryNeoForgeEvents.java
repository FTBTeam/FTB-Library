package dev.ftb.mods.ftblibrary.neoforge;

import dev.ftb.mods.ftblibrary.sidebar.RegisteredSidebarButton;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.Map;

public class FTBLibraryNeoForgeEvents {
    public static class CustomClickEvent extends Event implements ICancellableEvent {
        public final Identifier id;

        public CustomClickEvent(Identifier id) {
            this.id = id;
        }
    }

    public static class RegisterCustomColorEvent extends Event {
        public final Map<String, TextColor> colors;

        public RegisterCustomColorEvent(Map<String, TextColor> colors) {
            // Reference to the map
            this.colors = colors;
        }

        public void addColor(String id, TextColor color) {
            colors.put(id, color);
        }
    }

    public static class SidebarButtonCreatedEvent extends Event {
        public final RegisteredSidebarButton button;

        public SidebarButtonCreatedEvent(RegisteredSidebarButton button) {
            this.button = button;
        }
    }
}
