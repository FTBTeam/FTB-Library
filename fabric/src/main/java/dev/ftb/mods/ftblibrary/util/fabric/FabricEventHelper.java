package dev.ftb.mods.ftblibrary.util.fabric;

import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import net.fabricmc.fabric.api.event.Event;

import java.util.function.Consumer;

public class FabricEventHelper {
    public static <T> void registerFabricEventPoster(Class<T> cls, Event<? extends Consumer<T>> event) {
        EventPostingHandler.INSTANCE.registerEvent(cls, data -> event.invoker().accept(data));
    }
}
