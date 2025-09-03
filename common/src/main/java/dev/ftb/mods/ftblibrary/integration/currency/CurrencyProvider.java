package dev.ftb.mods.ftblibrary.integration.currency;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface CurrencyProvider {
    default boolean isValidProvider() {
        return true;
    }

    String getName();

    int getTotalCurrency(Player player);

    boolean takeCurrency(Player player, int amount);

    void giveCurrency(Player player, int amount);

    Component coinName(boolean plural);
}
