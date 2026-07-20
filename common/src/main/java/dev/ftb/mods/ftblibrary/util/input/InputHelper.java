package dev.ftb.mods.ftblibrary.util.input;

import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class InputHelper {
    /**
     * Register a keymapping with no default binding, in a platform-independent way. Convenience wrapper for
     * [Input#createKeyMapping(KeyMappingConfig)].
     *
     * @param name the keymap name
     * @param category the keymap category
     * @return the keymapping object
     */
    public static KeyMapping createSimpleKeyMapping(String name, String category) {
        return createSimpleKeyMapping(name, category, InputConstants.UNKNOWN.getValue());
    }

    /**
     * Register a keymapping a default binding and zero or more modifiers, in a platform-independent way. Convenience
     * wrapper for [Input#createKeyMapping(KeyMappingConfig)].
     *
     * @param name the keymap name
     * @param category the keymap category
     * @return the keymapping object
     * @implNote Although multiple modifiers are accepted, NeoForge keymapping objects only support a single modifier. So
     * it is not recommended to pass more than one modifier.
     */
    public static KeyMapping createSimpleKeyMapping(String name, String category, int keyCode, KeyModifier... modifiers) {
        KeyMappingConfig.Builder builder = KeyMappingConfig.builder(name, category).keyboard(keyCode);
        for (var m : modifiers) {
            builder.modifier(m);
        }
        return ClientUtils.input().createKeyMapping(builder.build());
    }
}
