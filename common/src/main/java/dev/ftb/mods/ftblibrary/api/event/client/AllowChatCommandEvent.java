package dev.ftb.mods.ftblibrary.api.event.client;

import java.util.function.Predicate;

@FunctionalInterface
public interface AllowChatCommandEvent extends Predicate<CustomClickEvent.Data> {
    record Data(String message) {
    }
}
