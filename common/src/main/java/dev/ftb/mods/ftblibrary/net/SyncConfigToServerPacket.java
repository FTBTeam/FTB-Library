package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public record SyncConfigToServerPacket(String configName, SNBTCompoundTag config) implements CustomPacketPayload {
    public static final Type<SyncConfigToServerPacket> TYPE = new Type<>(FTBLibrary.rl("sync_config_to_server_packet"));

    public static final StreamCodec<FriendlyByteBuf, SyncConfigToServerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncConfigToServerPacket::configName,
            dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag.STREAM_CODEC, SyncConfigToServerPacket::config,
            SyncConfigToServerPacket::new
    );

    public static SyncConfigToServerPacket create(SNBTConfig config) {
        return new SyncConfigToServerPacket(config.getKey(), Util.make(new SNBTCompoundTag(), config::write));
    }

    @Override
    public Type<SyncConfigToServerPacket> type() {
        return TYPE;
    }

    public static void handle(SyncConfigToServerPacket message, NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer sp && sp.hasPermissions(Commands.LEVEL_GAMEMASTERS)) {
            context.queue(() -> {
                MinecraftServer server = sp.getServer();

                ConfigManager.getInstance().syncFromClient(message.configName, message.config, sp.getGameProfile().getName());

                // send the updated config to all other players
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!sp.getUUID().equals(player.getUUID())) {
                        NetworkManager.sendToPlayer(player, new SyncConfigFromServerPacket(message.configName, message.config));
                    }
                }
            });
        }
    }
}
