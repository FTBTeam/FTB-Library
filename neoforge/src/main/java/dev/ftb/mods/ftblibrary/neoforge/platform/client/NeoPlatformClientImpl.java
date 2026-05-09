package dev.ftb.mods.ftblibrary.neoforge.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NeoPlatformClientImpl implements PlatformClient {
    private final Set<KeyMapping.Category> registeredCategories = ConcurrentHashMap.newKeySet();

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public void addResourcePackReloadListeners(String modId, Map<Identifier, PreparableReloadListener> listeners) {
        getModBusOrThrow(modId).addListener(AddClientReloadListenersEvent.class, event ->
                listeners.forEach(event::addListener));
    }

    @Override
    public KeyMapping.Category registerKeyMappingCategory(Identifier id) {
        var category = new KeyMapping.Category(id);
        if (!registeredCategories.add(category)) {
            throw new IllegalStateException("Key mapping category " + id + " is already registered");
        }

        return category;
    }

    @Override
    public void registerKeyMapping(String modId, KeyMapping... keyMappings) {
        getModBusOrThrow(modId).addListener(RegisterKeyMappingsEvent.class, event -> {
            for (var k : keyMappings) {
                event.register(k);
            }

            registeredCategories.forEach(event::registerCategory);
            registeredCategories.clear();
        });
    }

    private static @NonNull IEventBus getModBusOrThrow(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(ModContainer::getEventBus)
                .orElseThrow();
    }
}
