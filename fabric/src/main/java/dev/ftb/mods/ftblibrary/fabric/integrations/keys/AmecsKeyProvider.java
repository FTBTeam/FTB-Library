package dev.ftb.mods.ftblibrary.fabric.integrations.keys;

import dev.ftb.mods.ftblibrary.platform.client.keys.KeyConflict;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyModifier;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyMappingWithKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class AmecsKeyProvider implements KeyProvider {
    @Override
    public KeyMapping create(Identifier id, KeyMapping.Category category, Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> key, KeyModifier modifier, @Nullable Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> noModifierFallbackKey, KeyConflict conflictContext) {
        InputConstants.Type type = key.map(InputConstants.Key::getType, Pair::first);
        int value = key.map(InputConstants.Key::getValue, Pair::second);

        return new AmecsKeyMappingWithKeyModifiers(
                id,
                type,
                value,
                category,
                fromModifier(modifier)
        );
    }

    private AmecsKeyModifierCombination fromModifier(KeyModifier modifier) {
        return new AmecsKeyModifierCombination(
                modifier == KeyModifier.ALT,
                // I don't think this is really the correct behaviour tbh.
                modifier == KeyModifier.CONTROL || modifier == KeyModifier.SUPER,
                modifier == KeyModifier.SHIFT
        );
    }
}
