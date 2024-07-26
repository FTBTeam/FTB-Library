package dev.ftb.mods.ftblibrary.util;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;

public class NetworkHelper {
    public static void sendToAll(MessageType type, MinecraftServer server, FriendlyByteBuf buffer) {
        NetworkManager.sendToPlayers(server.getPlayerList().getPlayers(), type.getId(), buffer);
    }
}
