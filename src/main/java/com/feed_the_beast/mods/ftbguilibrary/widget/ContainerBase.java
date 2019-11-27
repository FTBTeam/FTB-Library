package com.feed_the_beast.mods.ftbguilibrary.widget;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class ContainerBase extends Container
{
	public ContainerBase(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory)
	{
		super(type, id);
	}

	public abstract int getNonPlayerSlots();

	@Override
	public boolean canInteractWith(PlayerEntity player)
	{
		return true;
	}

	public void addPlayerSlots(PlayerInventory playerInventory, int posX, int posY, boolean ignoreCurrent)
	{
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				addSlot(new Slot(playerInventory, x + y * 9 + 9, posX + x * 18, posY + y * 18));
			}
		}

		int i = ignoreCurrent ? playerInventory.currentItem : -1;

		for (int x = 0; x < 9; x++)
		{
			if (x != i)
			{
				addSlot(new Slot(playerInventory, x, posX + x * 18, posY + 58));
			}
			else
			{
				addSlot(new Slot(playerInventory, x, posX + x * 18, posY + 58)
				{
					@Override
					public boolean canTakeStack(PlayerEntity ep)
					{
						return false;
					}
				});
			}
		}
	}

	public void addPlayerSlots(PlayerInventory playerInventory, int posX, int posY)
	{
		addPlayerSlots(playerInventory, posX, posY, false);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int index)
	{
		int nonPlayerSlots = getNonPlayerSlots();

		if (nonPlayerSlots <= 0)
		{
			return ItemStack.EMPTY;
		}

		ItemStack stack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack stack1 = slot.getStack();
			stack = stack1.copy();

			if (index < nonPlayerSlots)
			{
				if (!mergeItemStack(stack1, nonPlayerSlots, inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!mergeItemStack(stack1, 0, nonPlayerSlots, false))
			{
				return ItemStack.EMPTY;
			}

			if (stack1.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return stack;
	}
}