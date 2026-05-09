package dev.ftb.mods.ftblibrary.neoforge.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyConflict;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyModifier;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.Pair;
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
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    @Override
    public KeyMapping createKeyBinding(Identifier id, KeyMapping.Category category, Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> key, KeyModifier modifier, @Nullable Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> noModifierFallbackKey, KeyConflict conflictContext) {
        InputConstants.Type type = key.map(InputConstants.Key::getType, Pair::first);
        int value = key.map(InputConstants.Key::getValue, Pair::second);

        return new KeyMapping(
                id.getNamespace() + "_" + id.getPath(),
                convertConflictContext(conflictContext),
                fromModifier(modifier),
                type,
                value,
                category
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
