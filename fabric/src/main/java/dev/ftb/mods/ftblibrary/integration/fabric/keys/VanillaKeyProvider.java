package dev.ftb.mods.ftblibrary.integration.fabric.keys;

import dev.ftb.mods.ftblibrary.util.input.KeyMappingConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class VanillaKeyProvider implements KeyProvider {
    @Override
    public KeyMapping create(KeyMappingConfig config) {
        return new KeyMapping(
                config.translationKey(),
                config.type(false),
                config.code(false),
                config.category()
        );
    }

    @Override
    public Component getKeyMappingDisplayName(KeyMapping keyMapping) {
        return keyMapping.getTranslatedKeyMessage();
    }

    @Override
    public boolean matchModifier(KeyMapping mapping) {
        // no modifiers supported by vanilla
        return true;
    }
}
