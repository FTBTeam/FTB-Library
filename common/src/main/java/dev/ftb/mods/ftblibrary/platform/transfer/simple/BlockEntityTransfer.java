package dev.ftb.mods.ftblibrary.platform.transfer.simple;

import io.netty.util.internal.UnstableApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * I do not recommend using this for anything more than the absolute simplest transfer interactions!
 */
@UnstableApi
public interface BlockEntityTransfer {
    NonNullList<ItemStack> getItems(Level level, BlockPos pos, Direction side);

    boolean putItems(List<ItemStack> items, Level level, BlockPos pos, Direction side);
}
