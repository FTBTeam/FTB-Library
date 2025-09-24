package dev.ftb.mods.ftblibrary.config.ui.resource;

import dev.architectury.registry.registries.RegistrarManager;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.SearchTerms;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Objects;

public class SelectItemStackScreen extends ResourceSelectorScreen<ItemStack> {
    public static final SearchModeIndex<ResourceSearchMode<ItemStack>> KNOWN_MODES = Util.make(
            new SearchModeIndex<>(),
            index -> {
                index.appendMode(ResourceSearchMode.ALL_ITEMS);
                index.appendMode(ResourceSearchMode.INVENTORY);
            }
    );

    public SelectItemStackScreen(ItemStackConfig config, ConfigCallback callback) {
        super(config, callback);
    }

    @Override
    protected SearchModeIndex<ResourceSearchMode<ItemStack>> getSearchModeIndex() {
        return KNOWN_MODES;
    }

    @Override
    protected ResourceSelectorScreen<ItemStack>.ResourceButton makeResourceButton(Panel panel, SelectableResource<ItemStack> resource) {
        return new ItemStackButton(panel, Objects.requireNonNullElse(resource, SelectableResource.item(ItemStack.EMPTY)));
    }

    private class ItemStackButton extends ResourceButton {
        private ItemStackButton(Panel panel, SelectableResource<ItemStack> resource) {
            super(panel, resource);
        }

        @Override
        public boolean shouldAdd(SearchTerms searchTerms) {
            return searchTerms.match(
                    RegistrarManager.getId(getResource().getItem(), Registries.ITEM),
                    getResource().getHoverName().getString(),
                    id -> getResource().is(TagKey.create(Registries.ITEM, id))
            );
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            if (!getResource().isEmpty()) {
                TooltipFlag flag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
                getResource().getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, flag).forEach(list::add);
                if (FTBLibraryClientConfig.ITEM_MODNAME.get()) {
                    ModUtils.getModName(getResource().getItem()).ifPresent(name ->
                            list.add(Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
                }
            }
        }
    }
}
