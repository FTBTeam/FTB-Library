package dev.ftb.mods.ftblibrary.util.client;

import dev.ftb.mods.ftblibrary.core.mixin.common.TextureAtlasAccess;
import dev.ftb.mods.ftblibrary.core.mixin.common.TextureManagerAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Resolves a bare sprite id to a sprite in whichever atlas contains it. Sprite ids are intended to be unique across
 * atlases; if one does appear in several, the {@code blocks} atlas wins, then the rest by atlas id.
 * <p>
 * The id-to-atlas map is rebuilt lazily after any atlas has been re-stitched (see {@link #invalidate()}).
 */
public final class AtlasSpriteLookup {
    private static final String ATLAS_TEXTURE_PREFIX = "textures/atlas/";
    private static final String ATLAS_TEXTURE_SUFFIX = ".png";

    private record AtlasRef(ResourceLocation id, TextureAtlas atlas) {}

    private record Snapshot(int generation, Map<ResourceLocation, AtlasRef> spriteToAtlas) {}

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
    public static TextureAtlasSprite find(ResourceLocation spriteId) {
        AtlasRef ref = currentSnapshot().spriteToAtlas().get(spriteId);
        if (ref == null) {
            return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(MissingTextureAtlasSprite.getLocation());
        }
        return ref.atlas().getSprite(spriteId);
    }

    /**
     * The id of the atlas (as in {@code assets/<ns>/atlases/<name>.json}) containing the given sprite, if any.
     */
    public static Optional<ResourceLocation> findAtlas(ResourceLocation spriteId) {
        return Optional.ofNullable(currentSnapshot().spriteToAtlas().get(spriteId)).map(AtlasRef::id);
    }

    /**
     * Visit every texture atlas which is currently loaded, with its atlas definition id (e.g. {@code minecraft:blocks}),
     * the blocks atlas first and the rest ordered by id. Atlases are those registered with the texture manager under
     * the conventional {@code textures/atlas/<name>.png} location. Render thread only.
     */
    public static void forEachAtlas(BiConsumer<ResourceLocation, TextureAtlas> consumer) {
        List<AtlasRef> atlases = new ArrayList<>();
        ((TextureManagerAccess) Minecraft.getInstance().getTextureManager()).getByPath().forEach((location, texture) -> {
            String path = location.getPath();
            if (texture instanceof TextureAtlas atlas && path.startsWith(ATLAS_TEXTURE_PREFIX) && path.endsWith(ATLAS_TEXTURE_SUFFIX)) {
                String name = path.substring(ATLAS_TEXTURE_PREFIX.length(), path.length() - ATLAS_TEXTURE_SUFFIX.length());
                atlases.add(new AtlasRef(ResourceLocation.fromNamespaceAndPath(location.getNamespace(), name), atlas));
            }
        });

        atlases.stream()
                .sorted(Comparator.<AtlasRef>comparingInt(r -> r.atlas().location().equals(InventoryMenu.BLOCK_ATLAS) ? 0 : 1)
                        .thenComparing(AtlasRef::id))
                .forEach(r -> consumer.accept(r.id(), r.atlas()));
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
        Map<ResourceLocation, AtlasRef> map = new HashMap<>();

        // forEachAtlas visits the preferred atlas first, so putIfAbsent gives it priority
        forEachAtlas((id, atlas) -> ((TextureAtlasAccess) atlas).getTexturesByName().keySet().forEach(sprite -> {
            if (!sprite.equals(MissingTextureAtlasSprite.getLocation())) {
                map.putIfAbsent(sprite, new AtlasRef(id, atlas));
            }
        }));

        return new Snapshot(generation, Map.copyOf(map));
    }
}
