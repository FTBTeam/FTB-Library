package dev.ftb.mods.ftblibrary.fabric.integrations.keys;

import dev.ftb.mods.ftblibrary.platform.client.keys.KeyConflict;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyModifier;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class VanillaKeyProvider implements KeyProvider {
    @Override
    public KeyMapping create(Identifier id, KeyMapping.Category category, Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> key, KeyModifier modifier, @Nullable Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> noModifierFallbackKey, KeyConflict conflictContext) {
        var selectedEither = noModifierFallbackKey != null ? noModifierFallbackKey : key;

        return new KeyMapping(
                id.getNamespace() + "_" + id.getPath(),
                selectedEither.map(InputConstants.Key::getType, Pair::first),
                selectedEither.map(InputConstants.Key::getValue, Pair::second),
                category
        );
    }
}
