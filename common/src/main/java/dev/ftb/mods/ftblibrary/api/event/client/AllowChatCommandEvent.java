package dev.ftb.mods.ftblibrary.api.event.client;

import dev.ftb.mods.ftblibrary.platform.event.TypedEvent;

import java.util.function.Predicate;

@FunctionalInterface
public interface AllowChatCommandEvent extends Predicate<CustomClickEvent.Data> {
    TypedEvent<Data, Boolean> TYPE = TypedEvent.ofBoolean(Data.class);

    record Data(String message) {
    }
}
