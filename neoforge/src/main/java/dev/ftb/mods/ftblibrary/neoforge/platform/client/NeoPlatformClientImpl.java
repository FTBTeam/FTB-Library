package dev.ftb.mods.ftblibrary.neoforge.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyConflict;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyMappingConfig;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyModifier;
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
import org.apache.commons.lang3.Validate;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NeoPlatformClientImpl implements PlatformClient {
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
    public void registerKeyMapping(String modId, KeyMapping... keyMappings) {
        Validate.isTrue(keyMappings.length > 0, "must provide at least one keymapping");

        getModBusOrThrow(modId).addListener(RegisterKeyMappingsEvent.class, event -> {
            Set<KeyMapping.Category> cats = new HashSet<>();
            for (var k : keyMappings) {
                cats.add(k.getCategory());
                event.register(k);
            }
            cats.forEach(event::registerCategory);
        });
    }

    @Override
    public KeyMapping createKeyBinding(KeyMappingConfig config) {
        return new KeyMapping(
                config.translationKey(),
                convertConflictContext(config.conflictContext()),
                fromModifier(config.modifier()),
                config.type(true),
                config.code(true),
                config.category()
        );
    }

    private net.neoforged.neoforge.client.settings.KeyModifier fromModifier(KeyModifier modifier) {
        return switch (modifier) {
            case ALT -> net.neoforged.neoforge.client.settings.KeyModifier.ALT;
            case SHIFT -> net.neoforged.neoforge.client.settings.KeyModifier.SHIFT;
            case CONTROL -> net.neoforged.neoforge.client.settings.KeyModifier.CONTROL;
            case SUPER -> net.neoforged.neoforge.client.settings.KeyModifier.CONTROL_OR_COMMAND;
            case NONE -> net.neoforged.neoforge.client.settings.KeyModifier.NONE;
        };
    }

    private static @NonNull IEventBus getModBusOrThrow(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(ModContainer::getEventBus)
                .orElseThrow();
    }

    private IKeyConflictContext convertConflictContext(KeyConflict conflict) {
        return switch (conflict) {
            case IN_GAME -> KeyConflictContext.IN_GAME;
            case ANY_GUI -> KeyConflictContext.GUI;
            case EVERYWHERE -> KeyConflictContext.UNIVERSAL;
        };
    }
}
