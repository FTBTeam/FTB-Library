package dev.ftb.mods.ftblibrary.integration;

import dev.emi.emi.api.*;
import dev.emi.emi.api.widget.Bounds;
import dev.ftb.mods.ftblibrary.config.ui.ResourceSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

@EmiEntrypoint
public class EMIIntegration implements EmiPlugin {
    private static final ResourceSearchMode<ItemStack> EMI_ITEMS = new ResourceSearchMode<>() {

        @Override
        public Icon getIcon() {
            // https://github.com/emilyploszaj/emi/blob/1.20.4/xplat/src/main/resources/assets/emi/textures/gui/widgets.png (the grid towards the bottom)
            return Icon.getIcon(ResourceLocation.fromNamespaceAndPath("emi", "textures/gui/widgets.png"))
                    .withUV(17, 147, 14, 14, 256, 256);
        }

        @Override
        public MutableComponent getDisplayName() {
            return Component.translatable("ftblibrary.select_item.list_mode.emi");
        }

        @Override
        public Collection<? extends SelectableResource<ItemStack>> getAllResources() {
            return EmiApi.getIndexStacks().stream()
                    .map(item -> {
                        // Transform the EMI ItemStack into an ItemStack then push to a SelectableResource
                        return SelectableResource.item(item.getItemStack());
                    })
                    .toList();
        }
    };

    @Override
    public void initialize(EmiInitRegistry registry) {
        SelectItemStackScreen.KNOWN_MODES.prependMode(EMI_ITEMS);
    }

    @Override
    public void register(EmiRegistry registry) {
        // Convert the standard bounds to EMI's custom bounds
        registry.addGenericExclusionArea((screen, boundsConsumer) -> {
            var sidebarButtons = SidebarGroupGuiButton.lastDrawnArea;
            boundsConsumer.accept(new Bounds(sidebarButtons.getX(), sidebarButtons.getY(), sidebarButtons.getWidth(), sidebarButtons.getHeight()));
        });
    }
}
