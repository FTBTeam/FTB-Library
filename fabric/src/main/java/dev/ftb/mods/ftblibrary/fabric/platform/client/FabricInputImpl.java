package dev.ftb.mods.ftblibrary.fabric.platform.client;

import dev.ftb.mods.ftblibrary.fabric.integrations.keys.AmecsKeyProvider;
import dev.ftb.mods.ftblibrary.fabric.integrations.keys.KeyProvider;
import dev.ftb.mods.ftblibrary.fabric.integrations.keys.VanillaKeyProvider;
import dev.ftb.mods.ftblibrary.platform.client.input.Input;
import dev.ftb.mods.ftblibrary.platform.client.input.KeyMappingConfig;
import dev.ftb.mods.ftblibrary.util.Lazy;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.Validate;

import java.util.HashSet;
import java.util.Set;

public class FabricInputImpl implements Input {
    private final Lazy<KeyProvider> keyProvider = Lazy.of(FabricInputImpl::initKeyProvider);

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
        Set<KeyMapping.Category> cats = new HashSet<>();
        for (var k : keyMappings) {
            cats.add(k.getCategory());
            KeyMappingHelper.registerKeyMapping(k);
        }
        cats.forEach(c -> KeyMapping.Category.register(c.id()));
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
    public boolean matches(KeyMapping mapping, KeyEvent event) {
        return mapping.matches(event) && keyProvider.get().matchModifier(mapping, event);
    }
}
