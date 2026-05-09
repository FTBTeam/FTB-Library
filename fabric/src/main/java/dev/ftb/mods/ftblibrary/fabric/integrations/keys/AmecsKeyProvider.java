package dev.ftb.mods.ftblibrary.fabric.integrations.keys;

import dev.ftb.mods.ftblibrary.platform.client.keys.KeyMappingConfig;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyModifier;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyMappingWithKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import net.minecraft.client.KeyMapping;

public class AmecsKeyProvider implements KeyProvider {
    @Override
    public KeyMapping create(KeyMappingConfig config) {
        return new AmecsKeyMappingWithKeyModifiers(
                config.id(),
                config.type(true),
                config.code(true),
                config.category(),
                fromModifier(config.modifier())
        );
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
