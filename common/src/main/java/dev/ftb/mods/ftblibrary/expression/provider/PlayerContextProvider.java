package dev.ftb.mods.ftblibrary.expression.provider;

import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;

/// Context provider for a [Player] providing support for {@code player.} expressions.
public class PlayerContextProvider extends ContextProvider {
    private final Player player;

    public PlayerContextProvider(Player player) {
        super("player");
        this.player = player;
    }

    public boolean isOp() {
        return this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    public boolean hasStage(String stage) {
        return false;
    }

    public boolean hasTag(String tag) {
        return player.entityTags().contains(tag);
    }

    public String playerName() {
        return player.getName().getString();
    }
}
