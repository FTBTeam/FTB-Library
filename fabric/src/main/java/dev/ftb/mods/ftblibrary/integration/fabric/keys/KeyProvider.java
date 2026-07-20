package dev.ftb.mods.ftblibrary.integration.fabric.keys;

import dev.ftb.mods.ftblibrary.util.input.KeyMappingConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public interface KeyProvider {
    KeyMapping create(KeyMappingConfig config);

    Component getKeyMappingDisplayName(KeyMapping keyMapping);

    boolean matchModifier(KeyMapping mapping);
}
