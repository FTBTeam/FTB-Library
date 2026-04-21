package dev.ftb.mods.ftblibrary.platform;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public interface Misc {
    boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode);

    Component componentWithLinks(String message);

    /// Platform-independent method to clear a player's cached display name, which may have been modified by either
    /// the Forge `PlayerEvent.NameFormat` event, or our own Fabric `PlayerDisplayNameCallback` event.
    /// Call this whenever the data required by any handlers for these events changes.
    ///
    /// @param player the player in question
    void refreshDisplayName(Player player);

    /// How many "sub-units" are in a bucket? Defintion of a "sub-unit" depends on the mod loader; NeoForge uses
    /// millibuckets (mB), and Fabric uses droplets.
    long bucketFluidAmount();

    boolean isFakePlayer(Player player);

    boolean isRailBlock(Block block);

    boolean playerHasCorrectTool(Player player, BlockPos pos, BlockState state) ;

    boolean canAxeStrip(ItemStack stack);

    boolean canTillSoil(ItemStack stack);

    boolean canFlattenPath(ItemStack stack);

    boolean hasComponentPatch(ItemStack stack);

    MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor);
}
