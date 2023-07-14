package dev.ftb.mods.ftblibrary.integration.stages;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface StageProvider {
    boolean has(Player player, String stage);

    void add(ServerPlayer player, String stage);

    void remove(ServerPlayer player, String stage);

    String getName();
}
