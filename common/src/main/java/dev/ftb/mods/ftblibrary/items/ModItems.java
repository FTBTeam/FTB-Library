package dev.ftb.mods.ftblibrary.items;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistryRef;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModItems {
    private static final XRegistry<Item> ITEMS = XRegistry.create(FTBLibrary.MOD_ID, Registries.ITEM);
    private static final XRegistryRef<Item> ICON_ITEM = ITEMS.register("icon_item", () -> new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, FTBLibrary.id("icon_item")))));

    private static final XRegistry<CreativeModeTab> CREATIVE_MODE_TABS = XRegistry.create(FTBLibrary.MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final XRegistryRef<CreativeModeTab> FTB_LIBRARY_TAB = CREATIVE_MODE_TABS.register("ftb_library", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.ftbsuite.creative_tab"))
            .icon(() -> new ItemStack(ICON_ITEM.get()))
            .build());

    public static void init() {
        CREATIVE_MODE_TABS.init();
        ITEMS.init();
    }
}
