package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.config.serializer.Json5ConfigSerializer;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncConfigFromServerPacket(String configName, String config) implements CustomPacketPayload {
    public static final Type<SyncConfigFromServerPacket> TYPE = new Type<>(FTBLibrary.rl("sync_config_from_server_packet"));

    public static final StreamCodec<FriendlyByteBuf, SyncConfigFromServerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncConfigFromServerPacket::configName,
            ByteBufCodecs.STRING_UTF8, SyncConfigFromServerPacket::config,
            SyncConfigFromServerPacket::new
    );

    public static SyncConfigFromServerPacket create(Config config) {
        return new SyncConfigFromServerPacket(config.getKey(), Json5ConfigSerializer.serialize(config).getAsString());
    }

    @Override
    public Type<SyncConfigFromServerPacket> type() {
        return TYPE;
    }

    public static void handle(SyncConfigFromServerPacket message, PacketContext context) {
        context.enqueue(() -> {
            ConfigManager.getInstance().syncFromServer(message.configName, message.config);
        });
    }
}
