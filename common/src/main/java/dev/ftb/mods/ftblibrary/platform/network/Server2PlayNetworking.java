package dev.ftb.mods.ftblibrary.platform.network;

import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class Server2PlayNetworking {
    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        Platform.get().networking().sendToPlayer(player, payload);
    }

    public static void send(Collection<ServerPlayer> players, CustomPacketPayload payload) {
        players.forEach(p -> Platform.get().networking().sendToPlayer(p, payload));
    }

    public static void sendToAllPlayers(MinecraftServer server, CustomPacketPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Platform.get().networking().sendToPlayer(player, payload);
        }
    }
}
