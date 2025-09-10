package dev.ftb.mods.ftblibrary.config.ui.resource;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.ResourceConfigValue;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.SearchTerms;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SelectEntityFaceScreen extends ResourceSelectorScreen<EntityType<?>> {
    private static final SearchModeIndex<ResourceSearchMode<EntityType<?>>> KNOWN_MODES = Util.make(
            new SearchModeIndex<>(), idx -> idx.appendMode(ResourceSearchMode.ENTITY_FACES)
    );

    public SelectEntityFaceScreen(ResourceConfigValue<EntityType<?>> config, ConfigCallback callback) {
        super(config, callback);
    }

    @Override
    protected ResourceSelectorScreen<EntityType<?>>.ResourceButton makeResourceButton(Panel panel, @Nullable SelectableResource<EntityType<?>> resource) {
        return new EntityFaceButton(panel, Objects.requireNonNullElse(resource, EntityFaceResource.NONE));
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
            return selectable instanceof EntityFaceResource r
                    && searchTerms.match(r.getLocation(), r.getLocation().toString(), key -> getResource().is(TagKey.create(Registries.ENTITY_TYPE, key)));
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
