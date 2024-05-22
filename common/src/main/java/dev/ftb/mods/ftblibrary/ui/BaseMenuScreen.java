package dev.ftb.mods.ftblibrary.ui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class BaseMenuScreen<T extends AbstractContainerMenu> extends BaseScreen {
    protected final T menu;
    protected final Inventory playerInventory;

    public BaseMenuScreen(T menu, Inventory playerInventory) {
        this.menu = menu;
        this.playerInventory = playerInventory;
    }
}
