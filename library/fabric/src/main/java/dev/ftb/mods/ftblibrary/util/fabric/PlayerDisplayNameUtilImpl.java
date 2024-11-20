package dev.ftb.mods.ftblibrary.util.fabric;

import dev.ftb.mods.ftblibrary.fabric.PlayerDisplayNameCache;
import net.minecraft.world.entity.player.Player;

public class PlayerDisplayNameUtilImpl {
    public static void refreshDisplayName(Player player) {
        ((PlayerDisplayNameCache) player).clearCachedDisplayName();
    }
}
