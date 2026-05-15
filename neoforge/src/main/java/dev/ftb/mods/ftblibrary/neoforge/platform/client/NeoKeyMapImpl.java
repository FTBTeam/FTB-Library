package dev.ftb.mods.ftblibrary.neoforge.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.keys.KeyConflict;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyMap;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyMappingConfig;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyModifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.apache.commons.lang3.Validate;

import java.util.HashSet;
import java.util.Set;

import static dev.ftb.mods.ftblibrary.neoforge.platform.client.NeoPlatformClientImpl.getModBusOrThrow;

public class NeoKeyMapImpl implements KeyMap {
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

    @Override
    public Component getKeyMappingDisplayName(KeyMapping keyMapping) {
        return keyMapping.getKeyModifier().getCombinedName(keyMapping.getKey(), () -> keyMapping.getKey().getDisplayName());
    }

    @Override
    public boolean matches(KeyMapping mapping, KeyEvent event) {
        return mapping.matches(event) && matchModifier(mapping, event);
    }

    private boolean matchModifier(KeyMapping mapping, KeyEvent event) {
        return switch (mapping.getKeyModifier()) {
            case CONTROL, CONTROL_OR_COMMAND -> event.hasControlDown();
            case SHIFT -> event.hasShiftDown();
            case ALT -> event.hasAltDown();
            case NONE -> event.modifiers() == 0;
        };
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

    private IKeyConflictContext convertConflictContext(KeyConflict conflict) {
        return switch (conflict) {
            case IN_GAME -> KeyConflictContext.IN_GAME;
            case ANY_GUI -> KeyConflictContext.GUI;
            case EVERYWHERE -> KeyConflictContext.UNIVERSAL;
        };
    }
}
