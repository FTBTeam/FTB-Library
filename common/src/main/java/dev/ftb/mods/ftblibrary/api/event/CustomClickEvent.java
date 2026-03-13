package dev.ftb.mods.ftblibrary.api.event;

import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface CustomClickEvent {
    boolean onClicked(Data data);

    record Data(Identifier id) {
    }
}
