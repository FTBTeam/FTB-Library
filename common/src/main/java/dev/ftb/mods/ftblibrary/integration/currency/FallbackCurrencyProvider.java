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
    public long getTotalCurrency(Player player) {
        return 0L;
    }

    @Override
    public boolean takeCurrency(Player player, long amount) {
        return false;
    }

    @Override
    public void giveCurrency(Player player, long amount) {
    }

    @Override
    public Component coinName(boolean plural) {
        return Component.literal("C?");
    }
}
