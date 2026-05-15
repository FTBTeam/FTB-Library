package dev.ftb.mods.ftblibrary.fabric.integrations.keys;

import dev.ftb.mods.ftblibrary.platform.client.keys.KeyMappingConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
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
    public boolean matchModifier(KeyMapping mapping, KeyEvent event) {
        // no modifiers supported by vanilla
        return event.modifiers() == 0;
    }
}
