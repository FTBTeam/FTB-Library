package dev.ftb.mods.ftblibrary.platform.client.keys;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.KeyMapping;
import org.jspecify.annotations.Nullable;

public record KeyMappingConfig(
        String id,
        KeyMapping.Category category,
        Pair<InputConstants.Type, Integer> key,
        KeyModifier modifier,
        @Nullable Pair<InputConstants.Type, Integer> noModifierFallbackKey,
        KeyConflict conflictContext
) {
    public String translationKey() {
        return "key." + category.id().getNamespace() + "." + category.id().getPath() + "." + id;
    }

    public InputConstants.Type type(boolean supportsModifiers) {
        var selectedKey = !supportsModifiers && noModifierFallbackKey != null ? noModifierFallbackKey : key;
        return selectedKey.left();
    }

    public int code(boolean supportsModifiers) {
        var selectedKey = !supportsModifiers && noModifierFallbackKey != null ? noModifierFallbackKey : key;
        return selectedKey.right();
    }

    public static Builder builder(String id, KeyMapping.Category category) {
        return new Builder(id, category);
    }

    public static class Builder {
        private final String id;
        private final KeyMapping.Category category;

        // We only support one modifier even though the Fabric library supports multiple because NeoForge's KeyModifier only supports one
        // When on Vanilla / Fabric without a mod, this is completely ignored anyway.
        private KeyModifier modifier = KeyModifier.NONE;

        private Pair<InputConstants.Type, Integer> key = Pair.of(InputConstants.UNKNOWN.getType(), InputConstants.UNKNOWN.getValue());

        // If there is no modifier support on the current platform, this key will override the main key
        // as there may be a case where a key with modifier makes sense but without the modifier, it doesn't and another key is preferred.
        private @Nullable Pair<InputConstants.Type, Integer> noModifierFallbackKey = null;

        // This is ignored on Fabric even with the mod as this is a NeoForge concept.
        private KeyConflict conflictContext = KeyConflict.EVERYWHERE;

        private Builder(String id, KeyMapping.Category category) {
            this.id = id;
            this.category = category;
        }

        public Builder shift() {
            return modifier(KeyModifier.SHIFT);
        }

        public Builder control() {
            return modifier(KeyModifier.CONTROL);
        }

        public Builder superModifier() {
            return modifier(KeyModifier.SUPER);
        }

        public Builder alt() {
            return modifier(KeyModifier.ALT);
        }

        public Builder modifier(KeyModifier modifier) {
            this.modifier = modifier;
            return this;
        }

        public Builder key(InputConstants.Type type, int code) {
            this.key = Pair.of(type, code);
            return this;
        }

        public Builder key(InputConstants.Key key) {
            this.key = Pair.of(key.getType(), key.getValue());
            return this;
        }

        public Builder keyboard(int keyCode) {
            return key(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
        }

        public Builder mouse(int buttonCode) {
            return key(InputConstants.Type.MOUSE.getOrCreate(buttonCode));
        }

        public Builder scan(int buttonCode) {
            return key(InputConstants.Type.SCANCODE.getOrCreate(buttonCode));
        }

        public Builder noModifierFallbackKey(InputConstants.Key noModifierFallbackKey) {
            this.noModifierFallbackKey = Pair.of(noModifierFallbackKey.getType(), noModifierFallbackKey.getValue());
            return this;
        }

        public Builder noModifierFallbackKey(InputConstants.Type type, int code) {
            this.noModifierFallbackKey = Pair.of(type, code);
            return this;
        }

        public Builder noModifierFallbackKeyboard(int keyCode) {
            return noModifierFallbackKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
        }

        public Builder noModifierFallbackMouse(int buttonCode) {
            return noModifierFallbackKey(InputConstants.Type.MOUSE.getOrCreate(buttonCode));
        }

        public Builder noModifierFallbackScan(int buttonCode) {
            return noModifierFallbackKey(InputConstants.Type.SCANCODE.getOrCreate(buttonCode));
        }

        public Builder conflictContext(KeyConflict conflictContext) {
            this.conflictContext = conflictContext;
            return this;
        }

        public KeyMappingConfig build() {
            if (key == null) {
                throw new IllegalStateException("Key must be set");
            }

            return new KeyMappingConfig(id, category, key, modifier, noModifierFallbackKey, conflictContext);
        }
    }
}
