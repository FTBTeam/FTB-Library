package dev.ftb.mods.ftblibrary.config.ui;

import com.google.common.base.Suppliers;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.SetOfItemStack;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.function.Supplier;

public interface ItemSearchMode {
	/**
	 * The icon used to represent this mode, for example on buttons and other widgets.
	 */
	Icon getIcon();

	/**
	 * The name used to describe this mode.
	 */
	MutableComponent getDisplayName();

	/**
	 * Gets an *unfiltered* collection of all items available in the current search mode.
	 */
	Collection<ItemStack> getAllItems();

	ItemSearchMode ALL_ITEMS = new ItemSearchMode() {
		private final Supplier<Collection<ItemStack>> supplier = Suppliers.memoize(this::computeStacks);

		@Override
		public Icon getIcon() {
			return ItemIcon.getItemIcon(Items.COMPASS);
		}

		@Override
		public MutableComponent getDisplayName() {
			return Component.translatable("ftblibrary.select_item.list_mode.all");
		}

		@Override
		public Collection<ItemStack> getAllItems() {
			return supplier.get();
		}

		private Set<ItemStack> computeStacks() {
			CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, false, ClientUtils.registryAccess());
			return new SetOfItemStack(CreativeModeTabs.allTabs().stream()
					.flatMap(tab -> tab.getDisplayItems().stream())
					.toList());
		}
	};

	ItemSearchMode INVENTORY = new ItemSearchMode() {
		@Override
		public Icon getIcon() {
			return ItemIcon.getItemIcon(Items.CHEST);
		}

		@Override
		public MutableComponent getDisplayName() {
			return Component.translatable("ftblibrary.select_item.list_mode.inv");
		}

		@Override
		public Collection<ItemStack> getAllItems() {
			Player player = Minecraft.getInstance().player;
			if (player == null) {
				return Collections.emptySet();
			}

			var invSize = player.getInventory().getContainerSize();
			List<ItemStack> items = new ArrayList<>(invSize);
			for (var i = 0; i < invSize; i++) {
				var stack = player.getInventory().getItem(i);
				if (!stack.isEmpty()) {
					items.add(stack);
				}
			}
			return items;
		}
	};

}
