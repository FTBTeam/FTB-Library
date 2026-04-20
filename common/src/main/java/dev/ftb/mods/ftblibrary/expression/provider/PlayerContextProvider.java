package dev.ftb.mods.ftblibrary.expression.provider;

import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.function.Function;

/// Context provider for a [Player] providing support for `player.` expressions.
public class PlayerContextProvider extends ContextProvider {
    private final Player player;

    public PlayerContextProvider(Player player) {
        super("player");
        this.player = player;
    }

    /// Checks if the player is an OP
    public boolean isOp() {
        return this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    public boolean hasStage(String stage) {
        return StageHelper.getInstance().getProvider().has(this.player, stage);
    }

    public boolean isCreative() {
        return this.player.isCreative();
    }

    public boolean isSpectator() {
        return this.player.isSpectator();
    }

    public boolean isSurvival() {
        return this.player.gameMode() == GameType.SURVIVAL;
    }

    public boolean isAdventure() {
        return this.player.gameMode() == GameType.ADVENTURE;
    }

    public boolean isSleeping() {
        return this.player.isSleeping();
    }

    public boolean isSwimming() {
        return this.player.isSwimming();
    }

    public boolean inContainer() {
        return this.player.containerMenu != this.player.inventoryMenu;
    }

    public String dimensionId() {
        //noinspection resource
        return this.player.level().dimension().identifier().toString();
    }

    public String dimensionNamespace() {
        //noinspection resource
        return this.player.level().dimension().identifier().getNamespace();
    }

    public String dimensionPath() {
        //noinspection resource
        return this.player.level().dimension().identifier().getPath();
    }

    /// Checks if the player has the given tag
    public boolean hasTag(String tag) {
        return player.entityTags().contains(tag);
    }

    /// Checks if the player is an implementation of a Fake Player or not.
    public boolean isFake() {
        return Platform.get().misc().isFakePlayer(player);
    }

    public boolean atSpawn() {
        //noinspection resource
        if (player.level() instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD && getSpawnProtectionRadius(serverLevel.getServer()) > 0) {
            BlockPos spawn = serverLevel.getRespawnData().pos();
            int x = Mth.abs(Mth.floor(player.getX()) - spawn.getX());
            int z = Mth.abs(Mth.floor(player.getZ()) - spawn.getZ());
            return Math.max(x, z) <= getSpawnProtectionRadius(serverLevel.getServer());
        }

        return false;
    }

    public String playerName() {
        return player.getName().getString();
    }

    public int getPlayTime() {
        return fromServerPlayerOrDefault(player, (serverPlayer) -> serverPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)), -1);
    }

    /// Helper to figure out the spawn protection radius, which is only available on a dedicated server instance
    private static int getSpawnProtectionRadius(MinecraftServer server) {
        return server instanceof DedicatedServer d ? d.spawnProtectionRadius() : 0;
    }

    private <T> T fromServerPlayerOrDefault(Player player, Function<ServerPlayer, T> function, T defaultValue) {
        if (player instanceof ServerPlayer serverPlayer) {
            return function.apply(serverPlayer);
        }

        return defaultValue;
    }
}
