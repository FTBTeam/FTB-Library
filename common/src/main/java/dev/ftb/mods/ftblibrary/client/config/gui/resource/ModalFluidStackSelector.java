package dev.ftb.mods.ftblibrary.client.config.gui.resource;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableFluid;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.SearchTerms;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;

import java.util.Objects;

public class ModalFluidStackSelector extends ModalResourceSelector<FluidStack> {
    private static final SearchModeIndex<ResourceSearchMode<FluidStack>> KNOWN_MODES = Util.make(
            new SearchModeIndex<>(), idx -> idx.appendMode(ResourceSearchMode.ALL_FLUIDS)
    );

    public ModalFluidStackSelector(Panel parent, EditableFluid config, ConfigCallback callback) {
        super(parent, config, callback);
    }

    @Override
    protected int defaultQuantity() {
        return (int) Platform.get().misc().bucketFluidAmount();
    }

    @Override
    protected SearchModeIndex<ResourceSearchMode<FluidStack>> getSearchModeIndex() {
        return KNOWN_MODES;
    }

    @Override
    protected ResourceButton makeResourceButton(Panel panel, SelectableResource<FluidStack> resource) {
        return new FluidStackButton(panel, Objects.requireNonNullElse(resource, SelectableResource.fluid(FluidStack.empty())));
    }

    private class FluidStackButton extends ResourceButton {
        private FluidStackButton(Panel panel, SelectableResource<FluidStack> resource) {
            super(panel, resource);
        }

        @Override
        public boolean shouldAdd(SearchTerms searchTerms) {
            var resourceId = BuiltInRegistries.FLUID.getKey(getResource().fluid());
            return searchTerms.match(resourceId,
                    getResource().name().getString(),
                    id -> getResource().fluid().builtInRegistryHolder().is(TagKey.create(Registries.FLUID, id))
            );
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (!getResource().isEmpty()) {
                list.add(getResource().name());
                if (FTBLibraryClientConfig.FLUID_MODNAME.get()) {
                    ModUtils.getModName(getResource().fluid())
                            .ifPresent(name -> list.add(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
                }
            }
        }
    }
}
