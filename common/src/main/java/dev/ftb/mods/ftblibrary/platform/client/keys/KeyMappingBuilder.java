package dev.ftb.mods.ftblibrary.platform.client.keys;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class KeyMappingBuilder {
    private final Identifier id;
    private final KeyMapping.Category category;

    // We only support one modifier even though the Fabric library supports multiple because NeoForge's KeyModifier only supports one
    // When on Vanilla / Fabric without a mod, this is completely ignored anyway.
    private KeyModifier modifier = KeyModifier.NONE;

    private Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> key;

    // If there is no modifier support on the current platform, this key will override the main key
    // as there may be a case where a key with modifier makes sense but without the modifier, it doesn't and another key is preferred.
    private @Nullable Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> noModifierFallbackKey = null;

    // This is ignored on Fabric even with the mod as this is a NeoForge concept.
    private KeyConflict conflictContext = KeyConflict.EVERYWHERE;

    private KeyMappingBuilder(Identifier id, KeyMapping.Category category) {
        this.id = id;
        this.category = category;
    }

    public static KeyMappingBuilder create(Identifier id, KeyMapping.Category category) {
        return new KeyMappingBuilder(id, category);
    }

    public KeyMappingBuilder shift() {
        return modifier(KeyModifier.SHIFT);
    }

    public KeyMappingBuilder control() {
        return modifier(KeyModifier.CONTROL);
    }

    public KeyMappingBuilder superModifier() {
        return modifier(KeyModifier.SUPER);
    }

    public KeyMappingBuilder alt() {
        return modifier(KeyModifier.ALT);
    }

    public KeyMappingBuilder modifier(KeyModifier modifier) {
        this.modifier = modifier;
        return this;
    }

    public KeyMappingBuilder key(InputConstants.Key key) {
        this.key = Either.left(key);
        return this;
    }

    public KeyMappingBuilder noModifierFallbackKey(InputConstants.Key noModifierFallbackKey) {
        this.noModifierFallbackKey = Either.left(noModifierFallbackKey);
        return this;
    }

    public KeyMappingBuilder key(InputConstants.Type type, int code) {
        this.key = Either.right(Pair.of(type, code));
        return this;
    }

    public KeyMappingBuilder noModifierFallbackKey(InputConstants.Type type, int code) {
        this.noModifierFallbackKey = Either.right(Pair.of(type, code));
        return this;
    }

    public KeyMappingBuilder conflictContext(KeyConflict conflictContext) {
        this.conflictContext = conflictContext;
        return this;
    }

    public KeyMapping build() {
        if (key == null) {
            throw new IllegalStateException("Key must be set");
        }

        return PlatformClient.get().createKeyBinding(
                id,
                category,
                key,
                modifier,
                noModifierFallbackKey,
                conflictContext
        );
    }
}
