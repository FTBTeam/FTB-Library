package dev.ftb.mods.ftblibrary.integration.permissions;

import net.minecraft.server.level.ServerPlayer;

public class FallbackPermissionProvider implements PermissionProvider {
    @Override
    public int getIntegerPermission(ServerPlayer player, String nodeName, int def) {
        return def;
    }

    @Override
    public boolean getBooleanPermission(ServerPlayer player, String nodeName, boolean def) {
        return def;
    }

    @Override
    public String getStringPermission(ServerPlayer player, String nodeName, String def) {
        return def;
    }

    @Override
    public String getName() {
        return "FALLBACK";
    }
}
