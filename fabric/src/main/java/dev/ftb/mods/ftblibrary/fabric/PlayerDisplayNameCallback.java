package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface PlayerDisplayNameCallback {
    ResourceLocation EARLY = new ResourceLocation(FTBLibrary.MOD_ID, "early");
    ResourceLocation LATE = new ResourceLocation(FTBLibrary.MOD_ID, "late");

    Event<PlayerDisplayNameCallback> EVENT = EventFactory.createWithPhases(PlayerDisplayNameCallback.class,
            (listeners) -> ((player, oldDisplayName) -> {
                if (listeners.length == 0) {
                    return null;
                }
                for (PlayerDisplayNameCallback event : listeners) {
                    oldDisplayName = event.modifyDisplayName(player, oldDisplayName);
                }
                return oldDisplayName;
            }), EARLY, Event.DEFAULT_PHASE, LATE);

    Component modifyDisplayName(Player player, Component oldDisplayName);
}
