package dev.ftb.mods.ftblibrary.fabric.integrations.keys;

import dev.ftb.mods.ftblibrary.platform.client.keys.KeyMappingConfig;
import net.minecraft.client.KeyMapping;

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
}
