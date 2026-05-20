package dev.ftb.mods.ftblibrary.fabric.integrations.keys;

import dev.ftb.mods.ftblibrary.platform.client.input.KeyMappingConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public interface KeyProvider {
    KeyMapping create(KeyMappingConfig config);

    Component getKeyMappingDisplayName(KeyMapping keyMapping);

    boolean matchModifier(KeyMapping mapping, KeyEvent event);
}
