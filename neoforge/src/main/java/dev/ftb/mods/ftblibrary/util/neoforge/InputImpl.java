package dev.ftb.mods.ftblibrary.util.neoforge;

import dev.ftb.mods.ftblibrary.util.input.Input;
import dev.ftb.mods.ftblibrary.util.input.KeyConflict;
import dev.ftb.mods.ftblibrary.util.input.KeyMappingConfig;
import dev.ftb.mods.ftblibrary.util.input.KeyModifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.apache.commons.lang3.Validate;

public class InputImpl implements Input {
    @Override
    public void registerKeyMapping(String modId, KeyMapping... keyMappings) {
        Validate.isTrue(keyMappings.length > 0, "must provide at least one keymapping");

        getModBusOrThrow(modId).addListener(RegisterKeyMappingsEvent.class, event -> {
            for (var k : keyMappings) {
                event.register(k);
            }
        });
    }

    @Override
    public KeyMapping createKeyMapping(KeyMappingConfig config) {
        return new KeyMapping(
                config.translationKey(),
                convertConflictContext(config.conflictContext()),
                fromModifier(config.modifier()),
                config.type(true),
                config.code(true),
                config.category()
        );
    }

    @Override
    public Component getKeyMappingDisplayName(KeyMapping keyMapping) {
        return keyMapping.getKeyModifier().getCombinedName(keyMapping.getKey(), () -> keyMapping.getKey().getDisplayName());
    }

    @Override
    public boolean matches(KeyMapping mapping, int keyCode, int scanCode) {
        return mapping.matches(keyCode, scanCode) && matchModifier(mapping);
    }

    private boolean matchModifier(KeyMapping mapping) {
        return switch (mapping.getKeyModifier()) {
            case CONTROL -> Screen.hasControlDown();
            case SHIFT -> Screen.hasShiftDown();
            case ALT -> Screen.hasAltDown();
            case NONE -> !Screen.hasAltDown() && !Screen.hasControlDown() && !Screen.hasShiftDown();
        };
    }

    private net.neoforged.neoforge.client.settings.KeyModifier fromModifier(KeyModifier modifier) {
        return switch (modifier) {
            case ALT -> net.neoforged.neoforge.client.settings.KeyModifier.ALT;
            case SHIFT -> net.neoforged.neoforge.client.settings.KeyModifier.SHIFT;
            case CONTROL -> net.neoforged.neoforge.client.settings.KeyModifier.CONTROL;
            case NONE -> net.neoforged.neoforge.client.settings.KeyModifier.NONE;
        };
    }

    private IKeyConflictContext convertConflictContext(KeyConflict conflict) {
        return switch (conflict) {
            case IN_GAME -> KeyConflictContext.IN_GAME;
            case ANY_GUI -> KeyConflictContext.GUI;
            case EVERYWHERE -> KeyConflictContext.UNIVERSAL;
        };
    }

    static IEventBus getModBusOrThrow(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(ModContainer::getEventBus)
                .orElseThrow();
    }
}
