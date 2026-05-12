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
import org.apache.commons.lang3.Validate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        Validate.isTrue(keyMappings.length > 0, "must provide at least one keymapping");
        Set<KeyMapping.Category> cats = new HashSet<>();
        for (var k : keyMappings) {
            cats.add(k.getCategory());
            KeyMappingHelper.registerKeyMapping(k);
        }
        cats.forEach(c -> KeyMapping.Category.register(c.id()));
    }
}
