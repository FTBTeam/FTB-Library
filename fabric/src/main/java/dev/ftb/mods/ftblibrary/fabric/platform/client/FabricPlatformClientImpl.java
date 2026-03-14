package dev.ftb.mods.ftblibrary.fabric.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FabricPlatformClientImpl implements PlatformClient {
    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
