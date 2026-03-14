package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.config.gui.ChooseConfigScreen;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManagerClient;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.permissions.Permissions;

public record EditConfigChoicePacket(ConfigType configType, String clientConfig, String serverConfig, Component title) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EditConfigChoicePacket> TYPE = new CustomPacketPayload.Type<>(FTBLibrary.rl("edit_config_choice_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EditConfigChoicePacket> STREAM_CODEC = StreamCodec.composite(
            NetworkHelper.enumStreamCodec(ConfigType.class), EditConfigChoicePacket::configType,
            ByteBufCodecs.STRING_UTF8, EditConfigChoicePacket::clientConfig,
            ByteBufCodecs.STRING_UTF8, EditConfigChoicePacket::serverConfig,
            ComponentSerialization.STREAM_CODEC, EditConfigChoicePacket::title,
            EditConfigChoicePacket::new
    );

    @Override
    public CustomPacketPayload.Type<EditConfigChoicePacket> type() {
        return TYPE;
    }

    public static EditConfigChoicePacket choose(String clientConfig, String serverConfig, Component title) {
        return new EditConfigChoicePacket(ConfigType.CHOOSE, clientConfig, serverConfig, title);
    }

    public static EditConfigChoicePacket client(String clientConfig) {
        return new EditConfigChoicePacket(ConfigType.CLIENT, clientConfig, "", Component.empty());
    }

    public static EditConfigChoicePacket server(String serverConfig) {
        return new EditConfigChoicePacket(ConfigType.SERVER, "", serverConfig, Component.empty());
    }

    public static void handle(EditConfigChoicePacket message, PacketContext context) {
        switch (message.configType) {
            case CLIENT -> ConfigManagerClient.editConfig(message.clientConfig);
            case SERVER -> {
                if (context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                    ConfigManagerClient.editConfig(message.serverConfig());
                }
            }
            case CHOOSE -> {
                if (context.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                    ChooseConfigScreen.open(message);
                } else {
                    ConfigManagerClient.editConfig(message.clientConfig());
                }
            }
        }
    }

    public enum ConfigType {
        CLIENT,
        SERVER,
        CHOOSE
    }
}
