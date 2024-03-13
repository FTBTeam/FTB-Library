package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import net.minecraft.network.FriendlyByteBuf;

public class EditConfigPacket extends BaseS2CMessage {
    private final boolean isClientConfig;

    public EditConfigPacket(boolean isClientConfig) {
        this.isClientConfig = isClientConfig;
    }

    public EditConfigPacket(FriendlyByteBuf buf) {
        isClientConfig = buf.readBoolean();
    }

    @Override
    public MessageType getType() {
        return FTBLibraryNet.EDIT_CONFIG;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(isClientConfig);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        FTBLibraryClient.editConfig(isClientConfig);
    }
}
