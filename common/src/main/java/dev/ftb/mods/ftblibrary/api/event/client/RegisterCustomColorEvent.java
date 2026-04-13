package dev.ftb.mods.ftblibrary.api.event.client;

import net.minecraft.network.chat.TextColor;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

@FunctionalInterface
public interface RegisterCustomColorEvent extends Consumer<RegisterCustomColorEvent.Data> {
    record Data(Map<String, TextColor> colors) {
        public void addColor(String id, TextColor color) {
            colors.put(id, color);
        }

        @Override
        public Map<String, TextColor> colors() {
            return Collections.unmodifiableMap(colors);
        }
    }
}
