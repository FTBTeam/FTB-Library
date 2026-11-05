package dev.ftb.mods.ftblibrary.client.util;

import dev.ftb.mods.ftblibrary.mixin.common.TextureAtlasAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Resolves a bare sprite id to a sprite in whichever atlas contains it. Sprite ids are intended to be unique across
 * atlases; if one does appear in several, the {@code blocks} atlas wins, then {@code items}, then the rest by atlas id.
 * <p>
 * The id-to-atlas map is rebuilt lazily after any atlas has been re-stitched (see {@link #invalidate()}).
 */
public final class AtlasSpriteLookup {
    private record Snapshot(int generation, Map<Identifier, Identifier> spriteToAtlas) {}

    // bumped (by TextureAtlasMixin) whenever any atlas has its sprites replaced or cleared
    private static final AtomicInteger GENERATION = new AtomicInteger();

    private static volatile Snapshot snapshot = new Snapshot(-1, Map.of());

    private AtlasSpriteLookup() {
    }

    /**
     * Mark the id-to-atlas map as stale; it will be rebuilt on the next lookup. Called when an atlas is (re)stitched
     * or cleared; not intended to be called from elsewhere.
     */
    public static void invalidate() {
        GENERATION.incrementAndGet();
    }

    /**
     * Find the sprite for the given id. Returns the missing-texture sprite if no atlas contains it.
     */
    public static TextureAtlasSprite find(Identifier spriteId) {
        var atlasManager = Minecraft.getInstance().getAtlasManager();

        Identifier atlasId = currentSnapshot().spriteToAtlas().get(spriteId);
        if (atlasId == null) {
            return atlasManager.getAtlasOrThrow(AtlasIds.BLOCKS).missingSprite();
        }
        return atlasManager.getAtlasOrThrow(atlasId).getSprite(spriteId);
    }

    /**
     * The id of the atlas containing the given sprite, if any.
     */
    public static Optional<Identifier> findAtlas(Identifier spriteId) {
        return Optional.ofNullable(currentSnapshot().spriteToAtlas().get(spriteId));
    }

    private static Snapshot currentSnapshot() {
        Snapshot snap = snapshot;
        int gen = GENERATION.get();
        if (snap.generation() != gen) {
            // generation is read *before* scanning, so an invalidation during the scan leaves this snapshot stale
            snapshot = snap = rebuild(gen);
        }
        return snap;
    }

    private static Snapshot rebuild(int generation) {
        Map<Identifier, Identifier> map = new HashMap<>();
        var atlasManager = Minecraft.getInstance().getAtlasManager();

        // preferred atlases first, so putIfAbsent gives them priority
        addAtlas(map, AtlasIds.BLOCKS, atlasManager.getAtlasOrThrow(AtlasIds.BLOCKS));
        addAtlas(map, AtlasIds.ITEMS, atlasManager.getAtlasOrThrow(AtlasIds.ITEMS));
        atlasManager.forEach((id, atlas) -> addAtlas(map, id, atlas));

        return new Snapshot(generation, Map.copyOf(map));
    }

    private static void addAtlas(Map<Identifier, Identifier> map, Identifier atlasId, TextureAtlas atlas) {
        ((TextureAtlasAccess) atlas).getTexturesByName().keySet().forEach(sprite -> {
            if (!sprite.equals(MissingTextureAtlasSprite.getLocation())) {
                map.putIfAbsent(sprite, atlasId);
            }
        });
    }
}
