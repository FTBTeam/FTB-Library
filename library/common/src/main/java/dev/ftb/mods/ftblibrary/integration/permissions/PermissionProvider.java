package dev.ftb.mods.ftblibrary.integration.permissions;

import net.minecraft.server.level.ServerPlayer;

public interface PermissionProvider {
    int getIntegerPermission(ServerPlayer player, String nodeName, int def);

    boolean getBooleanPermission(ServerPlayer player, String nodeName, boolean def);

    String getStringPermission(ServerPlayer player, String nodeName, String def);

    String getName();
}
