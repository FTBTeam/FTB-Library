package dev.ftb.mods.ftblibrary.util.fabric;

import dev.ftb.mods.ftblibrary.integration.fabric.keys.AmecsKeyProvider;
import dev.ftb.mods.ftblibrary.integration.fabric.keys.KeyProvider;
import dev.ftb.mods.ftblibrary.integration.fabric.keys.VanillaKeyProvider;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftblibrary.util.input.Input;
import dev.ftb.mods.ftblibrary.util.input.KeyMappingConfig;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.Validate;

public class InputImpl implements Input {
    private final Lazy<KeyProvider> keyProvider = Lazy.of(InputImpl::initKeyProvider);

    private static KeyProvider initKeyProvider() {
        if (FabricLoader.getInstance().isModLoaded("amecs")) {
            return new AmecsKeyProvider();
        } else {
            return new VanillaKeyProvider();
        }
    }

    @Override
    public void registerKeyMapping(String modId, KeyMapping... keyMappings) {
        Validate.isTrue(keyMappings.length > 0, "must provide at least one keymapping");
        for (var k : keyMappings) {
            KeyBindingHelper.registerKeyBinding(k);
        }
    }

    @Override
    public KeyMapping createKeyMapping(KeyMappingConfig config) {
        return keyProvider.get().create(config);
    }

    @Override
    public Component getKeyMappingDisplayName(KeyMapping keyMapping) {
        return keyProvider.get().getKeyMappingDisplayName(keyMapping);
    }

    @Override
    public boolean matches(KeyMapping mapping, int keyCode, int scanCode) {
        return mapping.matches(keyCode, scanCode) && keyProvider.get().matchModifier(mapping);
    }
}
