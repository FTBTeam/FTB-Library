package dev.ftb.mods.ftblibrary.fabric.platform.transfer.simple;

import dev.ftb.mods.ftblibrary.platform.transfer.simple.BlockEntityTransfer;
import io.netty.util.internal.UnstableApi;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
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
public class FabricBlockEntityTransferImpl implements BlockEntityTransfer {
    @Override
    public NonNullList<ItemStack> getItems(Level level, BlockPos pos, Direction side) {
        NonNullList<ItemStack> items = NonNullList.create();

        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage != null) {
            storage.forEach(storageView -> {
                if (!storageView.isResourceBlank()) {
                    items.add(storageView.getResource().toStack((int) storageView.getAmount()));
                }
            });
        }

        return items;
    }

    @Override
    public boolean putItems(List<ItemStack> items, Level level, BlockPos pos, Direction side) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage == null || !storage.supportsInsertion()) {
            throw new IllegalArgumentException("No item storage found");
        }

        try (Transaction tx = Transaction.openOuter()) {
            int ok = 0;
            for (ItemStack stack : items) {
                if (storage.insert(ItemVariant.of(stack), Integer.MAX_VALUE, tx) == stack.getCount()) {
                    ok++;
                } else {
                    break;
                }
            }
            if (ok == items.size()) {
                tx.commit();
                return true;
            } else {
                tx.abort();
                return false;
            }
        }
    }
}
