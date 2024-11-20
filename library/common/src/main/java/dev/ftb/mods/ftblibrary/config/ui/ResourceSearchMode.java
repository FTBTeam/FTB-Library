package dev.ftb.mods.ftblibrary.config.ui;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface ResourceSearchMode<T> {
    ResourceSearchMode<ItemStack> ALL_ITEMS = new ResourceSearchMode<>() {
        private List<SelectableResource<ItemStack>> allItemsCache = null;

        @Override
        public Icon getIcon() {
            return Icons.COMPASS;
        }

        @Override
        public MutableComponent getDisplayName() {
            return Component.translatable("ftblibrary.select_item.list_mode.all");
        }

        @Override
        public Collection<? extends SelectableResource<ItemStack>> getAllResources() {
            if (allItemsCache == null) {
                CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, false, ClientUtils.registryAccess());
                allItemsCache = CreativeModeTabs.searchTab().getDisplayItems().stream()
                        .map(SelectableResource::item)
                        .toList();
            }
            return allItemsCache;
        }
    };
    ResourceSearchMode<ItemStack> INVENTORY = new ResourceSearchMode<>() {
        @Override
        public Icon getIcon() {
            return ItemIcon.getItemIcon(Items.CHEST);
        }

        @Override
        public MutableComponent getDisplayName() {
            return Component.translatable("ftblibrary.select_item.list_mode.inv");
        }

        @Override
        public Collection<? extends SelectableResource<ItemStack>> getAllResources() {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return Collections.emptySet();
            }

            var invSize = player.getInventory().getContainerSize();
            List<SelectableResource<ItemStack>> items = new ArrayList<>(invSize);
            for (var i = 0; i < invSize; i++) {
                var stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    items.add(SelectableResource.item(stack));
                }
            }
            return items;
        }
    };
    ResourceSearchMode<FluidStack> ALL_FLUIDS = new ResourceSearchMode<>() {
        private List<SelectableResource<FluidStack>> allFluidsCache = null;

        @Override
        public Icon getIcon() {
            return ItemIcon.getItemIcon(Items.COMPASS);
        }

        @Override
        public MutableComponent getDisplayName() {
            return Component.translatable("ftblibrary.select_fluid.list_mode.all");
        }

        @Override
        public Collection<? extends SelectableResource<FluidStack>> getAllResources() {
            if (allFluidsCache == null) {
                List<SelectableResource<FluidStack>> fluidstacks = new ArrayList<>();
                BuiltInRegistries.FLUID.forEach(f -> {
                    if (f.isSource(f.defaultFluidState())) {
                        fluidstacks.add(SelectableResource.fluid(FluidStack.create(f, FluidStackHooks.bucketAmount())));
                    }
                });
                allFluidsCache = List.copyOf(fluidstacks);
            }
            return allFluidsCache;
        }
    };

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
    Collection<? extends SelectableResource<T>> getAllResources();

}
