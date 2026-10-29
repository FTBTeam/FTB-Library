package dev.ftb.mods.ftblibrary.client.config.gui.resource;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableItemStack;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.SearchTerms;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Objects;

public class ModalItemStackSelector extends ModalResourceSelector<ItemStack> {
    public ModalItemStackSelector(Panel parent, EditableItemStack config, ConfigCallback callback) {
        super(parent, config, callback);
    }

    @Override
    protected SearchModeIndex<ResourceSearchMode<ItemStack>> getSearchModeIndex() {
        return SelectItemStackScreen.KNOWN_MODES;
    }

    @Override
    protected ResourceButton makeResourceButton(Panel panel, SelectableResource<ItemStack> resource) {
        return new ItemStackButton(panel, Objects.requireNonNullElse(resource, SelectableResource.item(ItemStack.EMPTY)));
    }

    private class ItemStackButton extends ResourceButton {
        private ItemStackButton(Panel panel, SelectableResource<ItemStack> resource) {
            super(panel, resource);
        }

        @Override
        public boolean shouldAdd(SearchTerms searchTerms) {
            Identifier resourceId = BuiltInRegistries.ITEM.getKey(getResource().getItem());
            return searchTerms.match(resourceId,
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
