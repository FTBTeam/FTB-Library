package dev.ftb.mods.ftblibrary.integration.currency;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * Abstraction layer for mods which offer a currency system
 */
public interface CurrencyProvider {
    /**
     * This should be left true in any non-fallback implementation.
     * @return true if this provider is a valid, useful, provider
     */
    @ApiStatus.NonExtendable
    default boolean isValidProvider() {
        return true;
    }

    /**
     * {@return a brief descriptive name for this provider}
     */
    String getName();

    /**
     * {@return the player's total currency}
     * @param player the player to check
     */
    int getTotalCurrency(Player player);

    /**
     * {@return true if the currency was actually taken, i.e. the player had sufficient funds}
     * @param player the player to take from
     * @param amount the amount to take
     */
    boolean takeCurrency(Player player, int amount);

    /**
     * Give some currency to the player.
     *
     * @param player the player to give to
     * @param amount the amount to give
     */
    void giveCurrency(Player player, int amount);

    /**
     * {@return a printable name for the currency in question, for tooltip/messaging purposes}
     * @param plural false to get the singular description, true for the plural description
     */
    Component coinName(boolean plural);
}
