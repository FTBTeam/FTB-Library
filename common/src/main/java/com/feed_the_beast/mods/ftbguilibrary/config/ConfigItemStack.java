package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectItemStack;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ConfigItemStack extends ConfigValue<ItemStack> {
    public final boolean singleItemOnly;
    public final boolean allowEmpty;

    public ConfigItemStack(boolean single, boolean empty) {
        singleItemOnly = single;
        allowEmpty = empty;
        defaultValue = ItemStack.EMPTY;
        value = ItemStack.EMPTY;
    }

    @Override
    public ItemStack copy(ItemStack value) {
        return value.isEmpty() ? ItemStack.EMPTY : value.copy();
    }

    @Override
    public Component getStringForGUI(@Nullable ItemStack v) {
        if (v == null || v.isEmpty()) {
            return ItemStack.EMPTY.getHoverName();
        } else if (v.getCount() <= 1) {
            return v.getHoverName();
        }

        return new TextComponent(v.getCount() + "x ").append(v.getHoverName());
    }

    @Override
    public void onClicked(MouseButton button, ConfigCallback callback) {
        if (getCanEdit()) {
            new GuiSelectItemStack(this, callback).openGui();
        }
    }
}