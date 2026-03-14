package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.config.serializer.SNBTConfigSerializer;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public record SyncConfigToServerPacket(String configName, CompoundTag config) implements CustomPacketPayload {
    public static final Type<SyncConfigToServerPacket> TYPE = new Type<>(FTBLibrary.rl("sync_config_to_server_packet"));

    public static final StreamCodec<FriendlyByteBuf, SyncConfigToServerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncConfigToServerPacket::configName,
            ByteBufCodecs.COMPOUND_TAG, SyncConfigToServerPacket::config,
            SyncConfigToServerPacket::new
    );

    public static SyncConfigToServerPacket create(Config config) {
        return new SyncConfigToServerPacket(config.getKey(), SNBTConfigSerializer.serialize(config));
    }

    @Override
    public Type<SyncConfigToServerPacket> type() {
        return TYPE;
    }

    public static void handle(SyncConfigToServerPacket message, PacketContext context) {
        if (context.player() instanceof ServerPlayer sp && sp.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            context.enqueue(() -> {
                MinecraftServer server = sp.level().getServer();

                ConfigManager.getInstance().syncFromClient(message.configName, message.config, sp.getGameProfile().name());

                // send the updated config to all other players
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!sp.getUUID().equals(player.getUUID())) {
                        Server2PlayNetworking.send(player, new SyncConfigFromServerPacket(message.configName, message.config));
                    }
                }
            });
        }
    }
}
