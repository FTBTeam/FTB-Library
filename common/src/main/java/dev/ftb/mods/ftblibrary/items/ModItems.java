package dev.ftb.mods.ftblibrary.items;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(FTBLibrary.MOD_ID, Registries.ITEM);
    private static final DeferredSupplier<Item> ICON_ITEM = ITEMS.register("icon_item", () -> new Item(new Item.Properties()));

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(FTBLibrary.MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final DeferredSupplier<CreativeModeTab> FTB_LIBRARY_TAB = CREATIVE_MODE_TABS.register("ftb_library", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.ftbsuite.creative_tab"))
            .icon(() -> new ItemStack(ICON_ITEM.get()))
            .build());

    public static void init() {
        CREATIVE_MODE_TABS.register();
        ITEMS.register();
    }
}
