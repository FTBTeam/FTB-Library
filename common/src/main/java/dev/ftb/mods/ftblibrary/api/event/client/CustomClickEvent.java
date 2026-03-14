package dev.ftb.mods.ftblibrary.api.event.client;

import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface CustomClickEvent {
    boolean onClicked(Data data);

    record Data(Identifier id) {
    }
}
