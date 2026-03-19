package dev.ftb.mods.ftblibrary.api.event.client;

import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

@FunctionalInterface
public interface CustomClickEvent extends Predicate<CustomClickEvent.Data> {
    record Data(Identifier id) {
    }
}
