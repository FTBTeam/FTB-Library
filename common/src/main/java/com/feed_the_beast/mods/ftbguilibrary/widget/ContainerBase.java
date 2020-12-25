package com.feed_the_beast.mods.ftbguilibrary.widget;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public abstract class ContainerBase extends AbstractContainerMenu {
    public ContainerBase(@Nullable MenuType<?> type, int id, Inventory playerInventory) {
        super(type, id);
    }

    public abstract int getNonPlayerSlots();

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void addPlayerSlots(Inventory playerInventory, int posX, int posY, boolean ignoreCurrent) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(playerInventory, x + y * 9 + 9, posX + x * 18, posY + y * 18));
            }
        }

        int i = ignoreCurrent ? playerInventory.selected : -1;

        for (int x = 0; x < 9; x++) {
            if (x != i) {
                addSlot(new Slot(playerInventory, x, posX + x * 18, posY + 58));
            } else {
                addSlot(new Slot(playerInventory, x, posX + x * 18, posY + 58) {
                    @Override
                    public boolean mayPickup(Player ep) {
                        return false;
                    }
                });
            }
        }
    }

    public void addPlayerSlots(Inventory playerInventory, int posX, int posY) {
        addPlayerSlots(playerInventory, posX, posY, false);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int nonPlayerSlots = getNonPlayerSlots();

        if (nonPlayerSlots <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack1 = slot.getItem();
            stack = stack1.copy();

            if (index < nonPlayerSlots) {
                if (!moveItemStackTo(stack1, nonPlayerSlots, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack1, 0, nonPlayerSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (stack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return stack;
    }
}