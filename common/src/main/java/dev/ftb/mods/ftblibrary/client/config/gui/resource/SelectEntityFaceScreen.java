package dev.ftb.mods.ftblibrary.client.config.gui.resource;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableEntityFace;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableResource;
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
import net.minecraft.world.entity.EntityType;

public class SelectEntityFaceScreen extends ResourceSelectorScreen<EntityType<?>> {
    private static final SearchModeIndex<ResourceSearchMode<EntityType<?>>> KNOWN_MODES = Util.make(
            new SearchModeIndex<>(), idx -> {
                idx.appendMode(ResourceSearchMode.ALL_LIVING_ENTITIES);
                idx.appendMode(ResourceSearchMode.HOSTILES);
                idx.appendMode(ResourceSearchMode.NEUTRALS);
                idx.appendMode(ResourceSearchMode.ANIMALS);
            }
    );

    public SelectEntityFaceScreen(EditableResource<EntityType<?>> config, ConfigCallback callback) {
        super(config, callback);
    }

    @Override
    protected EntityType<?> emptyResource() {
        return EditableEntityFace.NONE;
    }

    @Override
    protected ResourceSelectorScreen<EntityType<?>>.ResourceButton makeResourceButton(Panel panel, SelectableResource<EntityType<?>> resource) {
        return new EntityFaceButton(panel, resource);
    }

    @Override
    protected ResourceSelectorScreen<EntityType<?>>.ResourceButton makeEmptyResourceButton(Panel panel) {
        return new EntityFaceButton(panel, EntityFaceResource.NONE);
    }

    @Override
    protected SearchModeIndex<ResourceSearchMode<EntityType<?>>> getSearchModeIndex() {
        return KNOWN_MODES;
    }

    private class EntityFaceButton extends ResourceButton {
        protected EntityFaceButton(Panel panel, SelectableResource<EntityType<?>> resource) {
            super(panel, resource);
        }

        @Override
        public boolean shouldAdd(SearchTerms searchTerms) {
            if (selectable == EntityFaceResource.NONE) {
                return true;
            }
            return selectable instanceof EntityFaceResource r
                    && searchTerms.match(r.getLocation(), r.getLocation().toString(), key -> getResource().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, key)));
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (selectable == EntityFaceResource.NONE) {
                list.add(Component.translatable("gui.none"));
            } else {
                list.add(getResource().getDescription());
                if (selectable instanceof EntityFaceResource r && FTBLibraryClientConfig.ENTITY_MODNAME.get()) {
                    ModUtils.getModName(r.getLocation().getNamespace()).ifPresent(name ->
                            list.add(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
                }
            }
        }
    }
}
