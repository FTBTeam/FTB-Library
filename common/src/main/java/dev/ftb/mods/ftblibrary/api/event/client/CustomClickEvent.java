package dev.ftb.mods.ftblibrary.api.event.client;

import dev.ftb.mods.ftblibrary.platform.event.TypedEvent;
import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

@FunctionalInterface
public interface CustomClickEvent extends Predicate<CustomClickEvent.Data> {
    TypedEvent<Data, Boolean> TYPE = TypedEvent.ofBoolean(Data.class);

    record Data(Identifier id) {
    }
}
