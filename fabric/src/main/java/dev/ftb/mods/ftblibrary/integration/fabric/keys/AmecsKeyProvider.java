package dev.ftb.mods.ftblibrary.integration.fabric.keys;


import dev.ftb.mods.ftblibrary.util.input.KeyMappingConfig;
import dev.ftb.mods.ftblibrary.util.input.KeyModifier;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyMappingWithKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
    public boolean matchModifier(KeyMapping mapping) {
        var modifiers = asAmecsMapping(mapping).getDefaultAmecsKeyModifiers();
        return (Screen.hasAltDown() || !modifiers.getAlt())
                && (Screen.hasControlDown() || !modifiers.getControl())
                && (Screen.hasShiftDown() || !modifiers.getShift());
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
                modifier == KeyModifier.CONTROL,
                modifier == KeyModifier.SHIFT
        );
    }
}
