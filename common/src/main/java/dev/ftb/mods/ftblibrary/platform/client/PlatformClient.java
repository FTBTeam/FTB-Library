package dev.ftb.mods.ftblibrary.platform.client;

import dev.ftb.mods.ftblibrary.platform.client.keys.KeyConflict;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyModifier;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.ServiceLoader;

public interface PlatformClient {
    PlatformClient INSTANCE = ServiceLoader.load(PlatformClient.class).findFirst().orElseThrow();

    static PlatformClient get() {
        return INSTANCE;
    }

    void sendToServer(CustomPacketPayload payload);

    void addResourcePackReloadListeners(String modId, Map<Identifier, PreparableReloadListener> listeners);

    default void addResourcePackReloadListener(String modId, Identifier id, PreparableReloadListener listener) {
        addResourcePackReloadListeners(modId, Map.of(id, listener));
    }

    KeyMapping.Category registerKeyMappingCategory(Identifier id);

    void registerKeyMapping(String modId, KeyMapping... keyMappings);

    // TODO: This is not a graceful interface design. Holding 2 eithers is meh.
    KeyMapping createKeyBinding(Identifier id, KeyMapping.Category category, Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> key, KeyModifier modifier, @Nullable Either<InputConstants.Key, Pair<InputConstants.Type, Integer>> noModifierFallbackKey, KeyConflict conflictContext);
}
