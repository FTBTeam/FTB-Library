package dev.ftb.mods.ftblibrary.integration.stages;

import dev.ftb.mods.ftblibrary.net.SyncGameStagesMessage;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class EntityTagStageProvider implements StageProvider {
    @Override
    public boolean has(Player player, String stage) {
        return player.getTags().contains(stage);
    }

    @Override
    public void add(ServerPlayer player, String stage) {
        player.addTag(stage);
        Server2PlayNetworking.send(player, SyncGameStagesMessage.add(stage));
    }

    @Override
    public void remove(ServerPlayer player, String stage) {
        player.removeTag(stage);
        Server2PlayNetworking.send(player, SyncGameStagesMessage.remove(stage));
    }

    @Override
    public void sync(ServerPlayer player) {
        Server2PlayNetworking.send(player, SyncGameStagesMessage.fullSync(player));
    }

    @Override
    public String getName() {
        return "Entity Tags";
    }
}
