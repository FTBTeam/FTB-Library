package dev.ftb.mods.ftblibrary.neoforge.platform;

import dev.ftb.mods.ftblibrary.platform.Misc;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.extensions.IBaseRailBlockExtension;
import net.neoforged.neoforge.common.util.FakePlayer;

public class NeoMiscImpl implements Misc {
    @Override
    public boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
        return keyBinding.isActiveAndMatches(keyCode);
    }

    @Override
    public Component componentWithLinks(String message) {
        return CommonHooks.newChatWithLinks(message);
    }

    @Override
    public void refreshDisplayName(Player player) {
        player.refreshDisplayName();
    }

    @Override
    public long bucketFluidAmount() {
        return 1_000;
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer || (player instanceof ServerPlayer && player.getClass() != ServerPlayer.class);
    }

    @Override
    public boolean isRailBlock(Block block) {
        return block instanceof IBaseRailBlockExtension;
    }

    @Override
    public boolean playerHasCorrectTool(Player player, BlockPos pos, BlockState state) {
        return player.hasCorrectToolForDrops(state, player.level(), pos);
    }

    @Override
    public boolean canAxeStrip(ItemStack stack) {
        return stack.getItem().canPerformAction(stack, ItemAbilities.AXE_STRIP);
    }

    @Override
    public boolean canTillSoil(ItemStack stack) {
        return stack.getItem().canPerformAction(stack, ItemAbilities.HOE_TILL);
    }

    @Override
    public boolean canFlattenPath(ItemStack stack) {
        return stack.getItem().canPerformAction(stack, ItemAbilities.SHOVEL_FLATTEN);
    }

    @Override
    public boolean hasComponentPatch(ItemStack stack) {
        return !stack.isComponentsPatchEmpty();
    }

    @Override
    public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
        return state.getBlock().getMapColor(state, level, pos, defaultColor);
    }
}
