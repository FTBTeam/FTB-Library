package dev.ftb.mods.ftblibrary.api.event.client;

import java.util.function.Consumer;

@FunctionalInterface
public interface AllowChatCommandEvent extends Consumer<CustomClickEvent.Data> {
    record Data(String message) {
    }
}
