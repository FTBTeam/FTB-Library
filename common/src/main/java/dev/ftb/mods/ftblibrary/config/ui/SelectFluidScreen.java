package dev.ftb.mods.ftblibrary.config.ui;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidBucketHooks;
import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.FluidConfig;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Objects;

public class SelectFluidScreen extends ResourceSelectorScreen<FluidStack> {
    public static final SearchModeIndex<ResourceSearchMode<FluidStack>> KNOWN_MODES = new SearchModeIndex<>();
    static {
        KNOWN_MODES.appendMode(ResourceSearchMode.ALL_FLUIDS);
    }

    public SelectFluidScreen(FluidConfig config, ConfigCallback callback) {
        super(config, callback);
    }

    private class FluidStackButton extends ResourceButton {
        private FluidStackButton(Panel panel, SelectableResource<FluidStack> resource) {
            super(panel, resource);
        }

        @Override
        public boolean shouldAdd(String search) {
            search = search.toLowerCase();
            if (search.isEmpty()) {
                return true;
            } else if (search.startsWith("@")) {
                return RegistrarManager.getId(getStack().getFluid(), Registries.FLUID).getNamespace().contains(search.substring(1));
            } else if (search.startsWith("#") && ResourceLocation.isValidResourceLocation(search.substring(1))) {
                return getStack().getFluid().builtInRegistryHolder().is(TagKey.create(Registries.FLUID, new ResourceLocation(search.substring(1))));
            } else {
                return getStack().getName().getString().toLowerCase().contains(search);
            }
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (!getStack().isEmpty()) {
                list.add(getStack().getName());
                if (FTBLibraryClientConfig.FLUID_MODNAME.get()) {
                    ModUtils.getModName(getStack().getFluid())
                            .ifPresent(name -> list.add(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
                }
            }
        }
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
}
