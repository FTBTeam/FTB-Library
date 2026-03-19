package dev.ftb.mods.ftblibrary.fabric.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Map;

public class FabricPlatformClientImpl implements PlatformClient {
    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void addResourcePackReloadListeners(String modId, Map<Identifier, PreparableReloadListener> listeners) {
        listeners.forEach((id, listener) ->
                ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(id, listener));
    }

    @Override
    public void registerKeyMapping(String modId, KeyMapping... keyMappings) {
        for (var k : keyMappings) {
            KeyMappingHelper.registerKeyMapping(k);
        }
    }
}
