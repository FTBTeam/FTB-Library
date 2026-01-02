package dev.ftb.mods.ftblibrary.config.ui.resource;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ResourceConfigValue;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.SearchTerms;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SelectImageResourceScreen extends ResourceSelectorScreen<Identifier> {
    private static final SelectableResource<Identifier> NO_IMAGE = new ImageResource(ImageResourceConfig.NONE);

    private static final SearchModeIndex<ResourceSearchMode<Identifier>> KNOWN_MODES = Util.make(
            new SearchModeIndex<>(), idx -> idx.appendMode(ResourceSearchMode.IMAGES)
    );

    public SelectImageResourceScreen(ResourceConfigValue<Identifier> config, ConfigCallback callback) {
        super(config, callback);
    }

    @Override
    protected ResourceSelectorScreen<Identifier>.ResourceButton makeResourceButton(Panel panel, @Nullable SelectableResource<Identifier> resource) {
        return new ImageButton(panel, Objects.requireNonNullElse(resource, NO_IMAGE));
    }

    @Override
    protected SearchModeIndex<ResourceSearchMode<Identifier>> getSearchModeIndex() {
        return KNOWN_MODES;
    }

    public enum ResourceListener implements ResourceManagerReloadListener {
        INSTANCE;

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            ResourceSearchMode.IMAGES.clearCache();
        }
    }

    private class ImageButton extends ResourceButton {
        protected ImageButton(Panel panel, SelectableResource<Identifier> resource) {
            super(panel, resource);
        }

        @Override
        public boolean shouldAdd(SearchTerms searchTerms) {
            return searchTerms.match(getResource(), getResource().toString(), id -> false);
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            Component text = Component.literal(getResource().getNamespace()).withStyle(ChatFormatting.GOLD).append(":")
                    .append(Component.literal(getResource().getPath()).withStyle(ChatFormatting.YELLOW));
            list.add(text);
            if (FTBLibraryClientConfig.IMAGE_MODNAME.get()) {
                ModUtils.getModName(getResource().getNamespace())
                        .ifPresent(name -> list.add(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
            }
        }
    }

}
