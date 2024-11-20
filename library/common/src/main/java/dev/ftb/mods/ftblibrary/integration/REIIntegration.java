package dev.ftb.mods.ftblibrary.integration;

import dev.ftb.mods.ftblibrary.config.ui.ResourceSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.List;

public class REIIntegration implements REIClientPlugin {

    private static final ResourceSearchMode<ItemStack> REI_ITEMS = new ResourceSearchMode<>() {
        @Override
        public Icon getIcon() {
            return ItemIcon.getItemIcon(Items.GLOW_BERRIES);
        }

        @Override
        public MutableComponent getDisplayName() {
            return Component.translatable("ftblibrary.select_item.list_mode.rei");
        }

        @Override
        public Collection<? extends SelectableResource<ItemStack>> getAllResources() {
            return CollectionUtils.filterAndMap(
                    EntryRegistry.getInstance().getPreFilteredList(),
                    stack -> stack.getType().equals(VanillaEntryTypes.ITEM),
                    stack -> SelectableResource.item(stack.castValue())
            );
        }
    };

    static {
        SelectItemStackScreen.KNOWN_MODES.prependMode(REI_ITEMS);
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(AbstractContainerScreen.class, screen -> {
            Rect2i lastDrawnArea = SidebarGroupGuiButton.lastDrawnArea;
            Rectangle sidebar = new Rectangle(lastDrawnArea.getX(), lastDrawnArea.getY(), lastDrawnArea.getWidth(), lastDrawnArea.getHeight());
            return List.of(sidebar);
        });
    }

}
