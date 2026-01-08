package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Util;

public record SyncConfigFromServerPacket(String configName, CompoundTag config) implements CustomPacketPayload {
    public static final Type<SyncConfigFromServerPacket> TYPE = new Type<>(FTBLibrary.rl("sync_config_from_server_packet"));

    public static final StreamCodec<FriendlyByteBuf, SyncConfigFromServerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncConfigFromServerPacket::configName,
            ByteBufCodecs.COMPOUND_TAG, SyncConfigFromServerPacket::config,
            SyncConfigFromServerPacket::new
    );

    public static SyncConfigFromServerPacket create(SNBTConfig config) {
        return new SyncConfigFromServerPacket(config.getKey(), Util.make(new SNBTCompoundTag(), config::write));
    }

    @Override
    public Type<SyncConfigFromServerPacket> type() {
        return TYPE;
    }

    public static void handle(SyncConfigFromServerPacket message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ConfigManager.getInstance().syncFromServer(message.configName, message.config);
        });
    }
}
