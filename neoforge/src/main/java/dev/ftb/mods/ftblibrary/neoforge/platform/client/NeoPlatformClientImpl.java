package dev.ftb.mods.ftblibrary.neoforge.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NeoPlatformClientImpl implements PlatformClient {

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
