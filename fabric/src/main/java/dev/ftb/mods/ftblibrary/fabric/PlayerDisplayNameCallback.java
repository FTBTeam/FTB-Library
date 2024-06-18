package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface PlayerDisplayNameCallback {
    ResourceLocation EARLY = FTBLibrary.rl("early");
    ResourceLocation LATE = FTBLibrary.rl("late");

    Event<PlayerDisplayNameCallback> EVENT = EventFactory.createWithPhases(PlayerDisplayNameCallback.class,
            (listeners) -> ((player, originalName) -> {
                // returning null means that no display name will be cached
                if (listeners.length == 0) {
                    return null;
                }
                Component modified = originalName;
                for (PlayerDisplayNameCallback event : listeners) {
                    modified = event.modifyDisplayName(player, modified);
                }
                // if no handler modified anything, don't do any caching
                return modified == originalName ? null : modified;
            }), EARLY, Event.DEFAULT_PHASE, LATE);

    Component modifyDisplayName(Player player, Component oldDisplayName);
}
