package dev.ftb.mods.ftblibrary.client.config.gui.resource;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableFluid;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.SearchTerms;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;

import java.util.Objects;

public class SelectFluidScreen extends ResourceSelectorScreen<FluidStack> {
    private static final SearchModeIndex<ResourceSearchMode<FluidStack>> KNOWN_MODES = Util.make(
            new SearchModeIndex<>(), idx -> idx.appendMode(ResourceSearchMode.ALL_FLUIDS)
    );

    public SelectFluidScreen(EditableFluid config, ConfigCallback callback) {
        super(config, callback);
    }

    @Override
    protected int defaultQuantity() {
        return (int) FluidStackHooks.bucketAmount();
    }

    @Override
    protected SearchModeIndex<ResourceSearchMode<FluidStack>> getSearchModeIndex() {
        return KNOWN_MODES;
    }

    @Override
    protected ResourceSelectorScreen<FluidStack>.ResourceButton makeResourceButton(Panel panel, SelectableResource<FluidStack> resource) {
        return new FluidStackButton(panel, Objects.requireNonNullElse(resource, SelectableResource.fluid(FluidStack.empty())));
    }

    private class FluidStackButton extends ResourceButton {
        private FluidStackButton(Panel panel, SelectableResource<FluidStack> resource) {
            super(panel, resource);
        }

        @Override
        public boolean shouldAdd(SearchTerms searchTerms) {
            return searchTerms.match(
                    RegistrarManager.getId(getResource().getFluid(), Registries.FLUID),
                    getResource().getName().getString(),
                    id -> getResource().getFluid().builtInRegistryHolder().is(TagKey.create(Registries.FLUID, id))
            );
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (!getResource().isEmpty()) {
                list.add(getResource().getName());
                if (FTBLibraryClientConfig.FLUID_MODNAME.get()) {
                    ModUtils.getModName(getResource().getFluid())
                            .ifPresent(name -> list.add(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
                }
            }
        }
    }
}
