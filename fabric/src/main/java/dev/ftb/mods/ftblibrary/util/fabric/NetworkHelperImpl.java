package dev.ftb.mods.ftblibrary.util.fabric;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class NetworkHelperImpl {
    public static void sendToAll(CustomPacketPayload.Type<?> type, MinecraftServer server, Packet<?> packet) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            sendTo(type, player, packet);
        }
    }

    public static void sendTo(CustomPacketPayload.Type<?> type, ServerPlayer player, Packet<?> packet) {
        if (ServerPlayNetworking.canSend(player, type)) {
            player.connection.send(packet);
        }
    }

    public static <T extends CustomPacketPayload> void sendToAll(MinecraftServer server, T packet) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            sendTo(player, packet);
        }
    }

    public static <T extends CustomPacketPayload> void sendTo(ServerPlayer player, T packet) {
        if (ServerPlayNetworking.canSend(player, packet.type())) {
            NetworkManager.sendToPlayer(player, packet);
        }
    }
}
