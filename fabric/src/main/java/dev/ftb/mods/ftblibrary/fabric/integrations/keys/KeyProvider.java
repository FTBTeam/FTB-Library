package dev.ftb.mods.ftblibrary.fabric.integrations.keys;

import dev.ftb.mods.ftblibrary.platform.client.keys.KeyMappingConfig;
import net.minecraft.client.KeyMapping;

public interface KeyProvider {
    KeyMapping create(KeyMappingConfig config);
}
