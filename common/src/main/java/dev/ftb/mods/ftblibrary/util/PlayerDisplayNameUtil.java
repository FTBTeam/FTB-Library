package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.world.entity.player.Player;

public class PlayerDisplayNameUtil {
    /**
     * Platform-independent method to clear a player's cached display name, which may have been modified by either
     * the Forge {@code PlayerEvent.NameFormat} event, or our own Fabric {@code PlayerDisplayNameCallback} event.
     * Call this whenever the data required by any handlers for these events changes.
     *
     * @param player the player in question
     */
    public static void refreshDisplayName(Player player) {
        Platform.get().misc().refreshDisplayName(player);
    }
}
