package dev.ftb.mods.ftblibrary.neoforge.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Map;

public class NeoPlatformClientImpl implements PlatformClient {
    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public void addResourcePackReloadListeners(String modId, Map<Identifier, PreparableReloadListener> listeners) {
        var modBus = ModList.get().getModContainerById(modId)
                .map(ModContainer::getEventBus)
                .orElseThrow();
        modBus.addListener(AddClientReloadListenersEvent.class, event -> listeners.forEach(event::addListener));
    }
}
