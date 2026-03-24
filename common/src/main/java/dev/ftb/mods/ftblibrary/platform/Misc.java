package dev.ftb.mods.ftblibrary.platform;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public interface Misc {
    boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode);

    Component componentWithLinks(String message);

    /**
     * Platform-independent method to clear a player's cached display name, which may have been modified by either
     * the Forge {@code PlayerEvent.NameFormat} event, or our own Fabric {@code PlayerDisplayNameCallback} event.
     * Call this whenever the data required by any handlers for these events changes.
     *
     * @param player the player in question
     */
    void refreshDisplayName(Player player);

    long bucketFluidAmount();

    boolean isFakePlayer(Player player);

    boolean isRailBlock(Block block);
}
