package dev.ftb.mods.ftblibrary.integration.currency;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public enum FallbackCurrencyProvider implements CurrencyProvider {
    INSTANCE;

    @Override
    public boolean isValidProvider() {
        return false;
    }

    @Override
    public String getName() {
        return "<NONE>";
    }

    @Override
    public int getTotalCurrency(Player player) {
        return 0;
    }

    @Override
    public boolean takeCurrency(Player player, int amount) {
        return false;
    }

    @Override
    public void giveCurrency(Player player, int amount) {
    }

    @Override
    public Component coinName(boolean plural) {
        return Component.literal("C?");
    }
}
