package dev.ftb.mods.ftblibrary.fabric.integrations.keys;

import de.siphalor.amecs.key_modifiers.api.AmecsKeyMappingWithKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiers;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyMappingConfig;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyModifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class AmecsKeyProvider implements KeyProvider {
    @Override
    public KeyMapping create(KeyMappingConfig config) {
        return new AmecsKeyMappingWithKeyModifiers(
                config.translationKey(),
                config.type(true),
                config.code(true),
                config.category(),
                fromModifier(config.modifier())
        );
    }

    @Override
    public Component getKeyMappingDisplayName(KeyMapping keyMapping) {
        return keyMapping.getTranslatedKeyMessage();
    }

    @Override
    public boolean matchModifier(KeyMapping mapping, KeyEvent event) {
        var modifiers = asAmecsMapping(mapping).getDefaultAmecsKeyModifiers();
        return (event.hasAltDown() || !modifiers.getAlt())
                && (event.hasControlDown() || !modifiers.getControl())
                && (event.hasShiftDown() || !modifiers.getShift());
    }

    private AmecsKeyMappingWithKeyModifiers asAmecsMapping(KeyMapping keyMapping) {
        if (keyMapping instanceof AmecsKeyMappingWithKeyModifiers amecs) {
            return amecs;
        }
        throw new IllegalStateException("using AmecsKeyProvider but keymapping is " + keyMapping.getClass());
    }

    private AmecsKeyModifierCombination fromModifier(KeyModifier modifier) {
        return new AmecsKeyModifierCombination(
                modifier == KeyModifier.ALT,
                // I don't think this is really the correct behaviour tbh.
                modifier == KeyModifier.CONTROL || modifier == KeyModifier.SUPER,
                modifier == KeyModifier.SHIFT
        );
    }
}
